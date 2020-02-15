package com.andb.apps.trails

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import com.andb.apps.trails.data.local.Database
import com.andb.apps.trails.data.local.Prefs
import com.andb.apps.trails.data.remote.Updater
import com.andb.apps.trails.data.repository.*
import com.andb.apps.trails.ui.area.AreaViewModel
import com.andb.apps.trails.ui.explore.ExploreFragment
import com.andb.apps.trails.ui.explore.ExploreViewModel
import com.andb.apps.trails.ui.favorites.FavoritesFragment
import com.andb.apps.trails.ui.favorites.FavoritesViewModel
import com.andb.apps.trails.ui.map.MapViewModel
import com.andb.apps.trails.ui.search.SearchFragment
import com.andb.apps.trails.ui.settings.SettingsFragment
import com.andb.apps.trails.ui.settings.SettingsViewModel
import com.andb.apps.trails.util.FileDownloader
import com.andb.apps.trails.util.newIoThread
import com.chibatching.kotpref.Kotpref
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import jonathanfinerty.once.Once
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class App : Application() {

    private val koinModule = module {
        single {
            Room.databaseBuilder(androidContext(), Database::class.java, "TrailsDatabase")
                .fallbackToDestructiveMigration()
                .build()
        }
        single { val db: Database = get(); db.regionsDao() }
        single { val db: Database = get(); db.areasDao() }
        single { val db: Database = get(); db.mapsDao() }

        single<RegionsRepository> { RegionsRepositoryImpl(get()) }
        single<AreasRepository> { AreasRepositoryImpl(get()) }
        single<MapsRepository> { MapsRepositoryImpl(get()) }

        single { FileDownloader(androidContext()) }

        single { FavoritesFragment() }
        single { ExploreFragment() }
        single { SearchFragment() }
        single { SettingsFragment() }

        viewModel { MainActivityViewModel(favoritesFragment = get(), exploreFragment = get(), searchFragment = get()) }
        viewModel { FavoritesViewModel(areasRepo = get(), mapsRepo = get()) }
        viewModel { ExploreViewModel(regionsRepo = get(), areasRepo = get()) }
        viewModel { AreaViewModel(regionsRepo = get(), areasRepo = get(), mapsRepo = get()) }
        viewModel { MapViewModel(areasRepo = get(), mapsRepo = get(), fileDownloader = get()) }
        viewModel { SettingsViewModel(androidContext()) }

        single {
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        }
        single {
            OkHttpClient.Builder()
                .apply {
                    //if(BuildConfig.DEBUG) addInterceptor(MockInterceptor(androidContext()))
                }
                .build()
        }
        single {
            Retrofit.Builder()
                .baseUrl("https://trailsbackend.ml/")
                .client(get())
                .addConverterFactory(MoshiConverterFactory.create(get()))
                .build()
        }


    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(koinModule)
        }
        Once.initialise(this)
        Kotpref.init(this)
        AppCompatDelegate.setDefaultNightMode(Prefs.nightMode)

        Log.d("once", "regionLoad: ${Once.beenDone(TimeUnit.DAYS, 1, "regionLoad")}")
        Log.d("once", "areaLoad: ${Once.beenDone(TimeUnit.DAYS, 1, "areaLoad")}")
        Log.d("once", "mapLoad: ${Once.beenDone(TimeUnit.DAYS, 1, "mapLoad")}")

        if (Updater.regionUpdateNeeded()) {
            newIoThread {
                Updater.updateRegions()
            }
        }

        if (Updater.areaUpdateNeeded()) {
            newIoThread {
                Updater.updateAreas()
            }
        }

        if (Updater.mapUpdateNeeded()) {
            newIoThread {
                Updater.updateMaps()
            }
        }
    }
}