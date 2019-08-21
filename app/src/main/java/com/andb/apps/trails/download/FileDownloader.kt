package com.andb.apps.trails.download

import android.content.Context
import android.os.Environment

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

object FileDownloader {
    private val MEGABYTE = 1024 * 1024

    private fun downloadFromURL(fileUrl: String, directory: File) {
        try {

            val url = URL(fileUrl)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()

            val inputStream = urlConnection.inputStream
            val fileOutputStream = FileOutputStream(directory)

            val buffer = ByteArray(MEGABYTE)
            var bufferLength = inputStream.read(buffer)
            while (bufferLength > 0) {
                fileOutputStream.write(buffer, 0, bufferLength)
                bufferLength = inputStream.read(buffer)
            }
            fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun downloadFile(context: Context, fileUrl: String, fileName: String, external: Boolean = false): File {
        val folder = File(context.filesDir.toString(), "PDFs")
        folder.mkdir()

        val pdfFile = File(folder, fileName)

        try {
            if (pdfFile.exists()) {
                return pdfFile
            } else {
                pdfFile.createNewFile()
                downloadFromURL(fileUrl, pdfFile)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return pdfFile
    }


}


