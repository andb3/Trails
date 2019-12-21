package com.andb.apps.trails.util.glide

import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException

class OkHttpProgressResponseBody internal constructor(
    private val url: HttpUrl,
    private val responseBody: ResponseBody,
    private val progressListener: ResponseProgressListener
) : ResponseBody() { //1

    //2
    private var bufferedSource: BufferedSource? = null

    //3
    override fun contentType(): MediaType {
        return responseBody.contentType()!!
    }

    //4
    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    //5
    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()))
        }
        return this.bufferedSource!!
    }

    //6
    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                val fullLength = responseBody.contentLength()
                if (bytesRead.toInt() == -1) { // this source is exhausted
                    totalBytesRead = fullLength
                } else {
                    totalBytesRead += bytesRead
                }
                progressListener.update(url, totalBytesRead, fullLength)  //7
                return bytesRead
            }
        }
    }
}