package com.andb.apps.trails

import android.app.Application
import android.os.AsyncTask
import com.andb.apps.trails.database.Database
import com.andb.apps.trails.download.setupRegions
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import jonathanfinerty.once.Once

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Once.initialise(this)
        Database.setDB(this)
        val config = ImageLoaderConfiguration.Builder(this)
            .defaultDisplayImageOptions(DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_map_black_24dp).build())
            .build()
        ImageLoader.getInstance().init(config)
    }
}