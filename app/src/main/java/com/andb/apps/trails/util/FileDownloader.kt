package com.andb.apps.trails.util

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class FileDownloader(val context: Context) {

    private fun downloadFromURL(fileUrl: String, directory: File, onProgress: ((Int) -> Unit)?) {
        try {

            val url = URL(fileUrl)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()


            val inputStream = urlConnection.inputStream
            val fileOutputStream = FileOutputStream(directory)

            var readLength = 0
            val buffer = ByteArray(MEGABYTE)
            var bufferLength = inputStream.read(buffer)
            while (bufferLength > 0) {
                readLength += bufferLength
                onProgress?.invoke((100 * readLength / urlConnection.contentLength))
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

    fun downloadFile(fileUrl: String, onProgress: (Int) -> Unit): File {
        val folder = File(context.filesDir.toString(), "PDFs")
        folder.mkdir()

        val pdfFile = File(folder, filenameFromURL(fileUrl))

        try {
            if (pdfFile.exists()) {
                return pdfFile
            } else {
                pdfFile.createNewFile()
                downloadFromURL(fileUrl, pdfFile, onProgress)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return pdfFile
    }

    fun downloadFileExternal(url: String, areaName: String, year: Int) {

        Log.d("downloadFileExternal", "start")

        val path = "${areaName}_${year}_${filenameFromURL(url)}"

        Log.d("downloadFileExternal", "path: $path")

        val pathToSave = context.getExternalFilesDir(null)
        val fileToSave = File(pathToSave, path)

        val notificationTitle = String.format(context.resources.getString(com.andb.apps.trails.R.string.downloading_notification_title), areaName, year)

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(notificationTitle)
            .setDescription(path)
            .setDestinationUri(Uri.fromFile(fileToSave))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val id = downloadManager.enqueue(request)

        Log.d("downloadFileExternal", "downloading id: $id")
    }

    companion object {
        private const val MEGABYTE = 1024 * 1024
    }
}


