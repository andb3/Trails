package com.andb.apps.trails.util.glide

import android.graphics.Bitmap
import android.util.Log
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

fun GlideRequest<Bitmap>.loadWithProgress(
    url: String?,
    granularity: Float = 1f,
    onProgress: (progress: Int) -> Unit
): GlideRequest<Bitmap> {
    Log.d("loadWithProgress", "loading url: $url")
    DispatchingProgressManager.expect(url, object : UIonProgressListener { //4

        override val granularityPercentage: Float //5
            get() = granularity

        override fun onProgress(bytesRead: Long, expectedLength: Long) {
            Log.d("onProgress", "invoking with ${(100 * bytesRead / expectedLength).toInt()}%")
            onProgress.invoke((100 * bytesRead / expectedLength).toInt())
        }
    })

    return this.load(url).addListener(object : RequestListener<Bitmap> { //9
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Bitmap>?,
            isFirstResource: Boolean
        ): Boolean {
            DispatchingProgressManager.forget(url)
            return false
        }

        override fun onResourceReady(
            resource: Bitmap?,
            model: Any?,
            target: Target<Bitmap>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            DispatchingProgressManager.forget(url)
            return false
        }
    })
}