package com.andb.apps.trails.download

import android.util.Log
import com.andb.apps.trails.xml.RegionXMLParser
import kotlinx.coroutines.*

fun setupRegions(): Boolean {


    return try {//bad code but catches


        CoroutineScope(Dispatchers.IO).launch {
            val jobs = ArrayList<Job>()
            for (i in 1..4) {
                val job = CoroutineScope(Dispatchers.IO).launch {
                    RegionXMLParser.parseParent(i)
                }
                jobs.add(job)
            }
            jobs.forEach {
                it.join()
            }
        }

        true
    } catch (e: Exception) {
        false
    }

}
