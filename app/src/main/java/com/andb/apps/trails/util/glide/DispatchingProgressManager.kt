package com.andb.apps.trails.util.glide

import android.os.Handler
import android.os.Looper
import okhttp3.HttpUrl

class DispatchingProgressManager internal constructor() : ResponseProgressListener {

    companion object {
        private val PROGRESSES = HashMap<String?, Long>() //1
        private val LISTENERS = HashMap<String?, UIonProgressListener>() //2

        internal fun expect(url: String?, listener: UIonProgressListener) { //3
            LISTENERS[url] = listener
        }

        internal fun forget(url: String?) { //4
            LISTENERS.remove(url)
            PROGRESSES.remove(url)
        }
    }

    private val handler: Handler = Handler(Looper.getMainLooper()) //5

    override fun update(url: HttpUrl, bytesRead: Long, contentLength: Long) {
        val key = url.toString()
        val listener = LISTENERS[key] ?: return //6
        if (contentLength <= bytesRead) { //7
            forget(key)
        }
        if (needsDispatch(
                key, bytesRead, contentLength,
                listener.granularityPercentage
            )
        ) { //8
            handler.post { listener.onProgress(bytesRead, contentLength) }
        }
    }

    private fun needsDispatch(
        key: String,
        current: Long,
        total: Long,
        granularity: Float
    ): Boolean {
        if (granularity == 0f || current == 0L || total == current) {
            return true
        }
        val percent = 100f * current / total
        val currentProgress = (percent / granularity).toLong()
        val lastProgress = PROGRESSES[key]
        return if (lastProgress == null || currentProgress != lastProgress) { //9
            PROGRESSES[key] = currentProgress
            true
        } else {
            false
        }
    }
}