package com.andb.apps.trails

import android.app.Application
import android.util.Log
import com.andb.apps.trails.database.Database
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.MapsRepo
import com.andb.apps.trails.repository.RegionsRepo
import com.andb.apps.trails.utils.newIoThread
import jonathanfinerty.once.Once
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Once.initialise(this)
        Database.setDB(this)

        if(!Once.beenDone(TimeUnit.DAYS, 1, "regionLoad")){
            RegionsRepo.loading.value = true
            newIoThread {
                Log.d("once", "updating regions")
                val loaded = RegionsRepo.updateRegions()
                if (loaded){
                    Once.markDone("regionLoad")
                }
                RegionsRepo.loading.postValue(false)
            }
        }

        if(!Once.beenDone(TimeUnit.DAYS, 1, "areaLoad")){
            AreasRepo.loading.value = true
            newIoThread {
                val loaded = AreasRepo.updateAreas()
                if (loaded){
                    Once.markDone("areaLoad")
                }
                AreasRepo.loading.postValue(false)
            }
        }

        if(!Once.beenDone(TimeUnit.DAYS, 1, "mapLoad")){
            MapsRepo.loading.value = true
            newIoThread {
                val loaded = MapsRepo.updateMaps()
                if (loaded){
                    Once.markDone("mapLoad")
                }
                MapsRepo.loading.postValue(false)
            }
        }
    }
}