package com.andb.apps.trails.util.glide

interface UIonProgressListener {
    val granularityPercentage: Float //1
    fun onProgress(bytesRead: Long, expectedLength: Long) //2
}