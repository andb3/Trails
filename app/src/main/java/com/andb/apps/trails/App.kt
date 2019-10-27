package com.andb.apps.trails

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import com.andb.apps.trails.database.Database
import com.andb.apps.trails.database.MapsDao
import com.andb.apps.trails.pages.*
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.MapsRepo
import com.andb.apps.trails.repository.MockInterceptor
import com.andb.apps.trails.repository.RegionsRepo
import com.andb.apps.trails.settings.SettingsFragment
import com.andb.apps.trails.settings.SettingsViewModel
import com.andb.apps.trails.utils.newIoThread
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

        single { FavoritesFragment() }
        single { ExploreFragment() }
        single { SearchFragment() }
        single { SettingsFragment() }

        viewModel { MainActivityViewModel(get(), get(), get()) }
        viewModel { FavoritesViewModel() }
        viewModel { ExploreViewModel() }
        viewModel { AreaViewModel() }
        viewModel { SettingsViewModel(androidContext() as Application) }

        single {
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        }
        single {
            OkHttpClient.Builder()
                .addInterceptor(MockInterceptor(androidContext()))
                .build()
        }
        single {
            Retrofit.Builder()
                .baseUrl("https://trailsbackend-254417.appspot.com/")
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
        Database.setDB(this)
        Kotpref.init(this)
        AppCompatDelegate.setDefaultNightMode(Prefs.nightMode)

        Log.d("once", "regionLoad: ${Once.beenDone(TimeUnit.DAYS, 1, "regionLoad")}")
        Log.d("once", "areaLoad: ${Once.beenDone(TimeUnit.DAYS, 1, "areaLoad")}")
        Log.d("once", "mapLoad: ${Once.beenDone(TimeUnit.DAYS, 1, "mapLoad")}")

        if (!Once.beenDone(TimeUnit.DAYS, 1, "regionLoad")) {
            RegionsRepo.loading.value = true
            newIoThread {
                Log.d("once", "updating regions")
                val loaded = RegionsRepo.updateRegions()
                if (loaded) {
                    Log.d("once", "updated regions")
                    Once.markDone("regionLoad")
                }
                RegionsRepo.loading.postValue(false)
            }
        }

        if (!Once.beenDone(TimeUnit.DAYS, 1, "areaLoad")) {
            AreasRepo.loading.value = true
            newIoThread {
                Log.d("once", "updating areas")
                val loaded = AreasRepo.updateAreas()
                if (loaded) {
                    Log.d("once", "updated areas")
                    Once.markDone("areaLoad")
                }
                AreasRepo.loading.postValue(false)
            }
        }

        if (!Once.beenDone(TimeUnit.DAYS, 1, "mapLoad")) {
            MapsRepo.loading.value = true
            newIoThread {
                val loaded = MapsRepo.updateMaps()
                if (loaded) {
                    Once.markDone("mapLoad")
                }
                MapsRepo.loading.postValue(false)
            }
        }
    }
}