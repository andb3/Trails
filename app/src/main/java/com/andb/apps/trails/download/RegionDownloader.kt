package com.andb.apps.trails.download

import com.andb.apps.trails.xml.RegionXMLParser
import kotlinx.coroutines.*

const val PARENT_REGIONS = 6
suspend fun setupRegions(service: InitialDownloadService): Boolean {


    return try {//bad code but catches


        CoroutineScope(Dispatchers.IO).launch {
            val jobs = ArrayList<Job>()
            for (i in 1..PARENT_REGIONS) {
                val job = CoroutineScope(Dispatchers.IO).launch {
                    RegionXMLParser.parseParent(i)
                }
                jobs.add(job)
            }
            jobs.forEach {
                it.join()
                service.updateProgress()
            }
        }

        true
    } catch (e: Exception) {
        false
    }

}
