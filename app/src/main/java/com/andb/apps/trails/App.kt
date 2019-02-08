package com.andb.apps.trails

import android.app.Application
import android.os.AsyncTask
import com.andb.apps.trails.database.Database
import com.andb.apps.trails.download.setupRegions
import io.alterac.blurkit.BlurKit
import jonathanfinerty.once.Once

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Once.initialise(this)
        Database.setDB(this)
        BlurKit.init(this)
    }
}