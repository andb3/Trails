package com.andb.apps.trails.download

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.util.Log
import com.andb.apps.trails.utils.filenameFromURL
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


object FileDownloader {
    private const val MEGABYTE = 1024 * 1024

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

    fun downloadFile(context: Context, fileUrl: String): File {
        val folder = File(context.filesDir.toString(), "PDFs")
        folder.mkdir()

        val pdfFile = File(folder, filenameFromURL(fileUrl))

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

    /*fun downloadFileExternal(context: Context, url: String, path: String){
        val pathToSave = context.getExternalFilesDir(null)
        pathToSave.mkdir()
        val fileToSave = File(pathToSave, path)

        if(fileToSave.exists()) return

        val possibleFile = File(File(context.filesDir.toString(), "PDFs"), filenameFromURL(url))
        if(possibleFile.exists()){
            val input = FileInputStream(possibleFile).channel
            val output = FileOutputStream(fileToSave).channel
            output.transferFrom(input, 0, input.size())
            input.close()
            output.close()
            return
        }

        downloadFromURL(url, fileToSave)
    }*/

    fun downloadFileExternal(context: Context, url: String, areaName: String, year: Int) {

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
}


