package com.andb.apps.trails.download

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.andb.apps.trails.MainActivity
import com.andb.apps.trails.R
import com.andb.apps.trails.xml.AreaXMLParser
import jonathanfinerty.once.Once
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

const val NOTIFICATION_ID = 23498
const val CHANNEL_ID = "trails_channel_id"

const val TAG_REGION_SETUP = "regionSetup"
const val TAG_AREA_SETUP = "areaSetup"

const val DOWNLOADING_REGIONS = 0
const val DOWNLOADING_AREAS = 1
const val DOWNLOADING_SUCCEEDED = 2
const val DOWNLOADING_FAILED = 3


class InitialDownloadService : Service() {
    private var maxItems: Int = PARENT_REGIONS
    private var itemCount = 0
    private var stage: Int = 0
    private lateinit var notificationBuilder: Notification.Builder
    private val notifManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            startDownload()
        }

    }

    private suspend fun startDownload() {
        createNotification()
        val regionsDownloaded = if(!Once.beenDone(TAG_REGION_SETUP)) downloadRegions() else true
        if (regionsDownloaded) {

            Once.markDone(TAG_REGION_SETUP)
            val areasDownloaded = if(!Once.beenDone(TAG_AREA_SETUP)) downloadAreas() else true

            if (areasDownloaded) {
                Once.markDone(TAG_AREA_SETUP)
                updateStage(DOWNLOADING_SUCCEEDED)
            }else{
                updateStage(DOWNLOADING_FAILED)
            }

        } else {
            updateStage(DOWNLOADING_FAILED)
        }
    }

    private suspend fun downloadRegions(): Boolean {
        return setupRegions(this)
    }

    private suspend fun downloadAreas(): Boolean {
        val nodeList = AreaXMLParser.getIndex()
        nextStage(nodeList.length*2)
        return updateAreas(this, nodeList)
    }


    //***********Notification Creation*****************//
    private fun createNotification() {

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channelDescription = "Trails"
            //Check if notification channel exists and if not create one
            var notificationChannel: NotificationChannel? = notifManager.getNotificationChannel(CHANNEL_ID)
            if (notificationChannel == null) {
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                notificationChannel = NotificationChannel(CHANNEL_ID, channelDescription, importance)
                notificationChannel.lightColor = Color.BLACK
                notificationChannel.enableVibration(true)
                notifManager.createNotificationChannel(notificationChannel)
            }
            notificationBuilder = Notification.Builder(this, CHANNEL_ID)

        } else {
            notificationBuilder = Notification.Builder(this)
        }

        notificationBuilder.setContentTitle(notifTitle(this, stage))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(maxItems, itemCount, false)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)



        val notification = notificationBuilder.build()
        updateNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotification() {
        Log.d("updateNotification", "updating to stage $stage")
        val notification: Notification
        val id: Int
        when(stage) {
            2, 3 -> {
                Log.d("updateNotification", "cancelling and re-adding")
                notifManager.cancel(NOTIFICATION_ID)
                notificationBuilder.setProgress(0, 0, false).setAutoCancel(true)
                notification = notificationBuilder.build().also { stopForeground(true) }
                id = NOTIFICATION_ID + stage
            }
            else -> {
                Log.d("updateNotification", "updating")
                id = NOTIFICATION_ID
                notification = notificationBuilder.build()
            }
        }
        notifManager.notify(id, notification)
    }

    fun updateProgress(amount: Int = 1) {
        itemCount += amount
        Log.d("updatingProgress", "itemCount: $itemCount")
        notificationBuilder.setProgress(maxItems, itemCount, false)
        updateNotification()
        EventBus.getDefault().post(DownloadEvent(progress = progress()))
    }

    private fun progress(): Int = (itemCount.toFloat() / maxItems).toInt() * 100


    private fun nextStage(items: Int) {
        stage++
        maxItems = items
        updateStage()
    }

    private fun updateStage(newStage: Int = this.stage) {
        this.stage = newStage
        Log.d("updateStage","updating to stage $stage: ${descFromStage(this, stage)}")
        itemCount = 0
        val newTitle = notifTitle(this, stage)
        Log.d("newTitle", newTitle)
        notificationBuilder.setContentTitle(newTitle)
        notificationBuilder.setProgress(maxItems, 0, false)
        updateNotification()
        EventBus.getDefault().post(DownloadEvent(status = stage))
    }


}

 fun notifTitle(context: Context, stage: Int): String {
    return context.getString(R.string.download_progress_title) + ": " + descFromStage(context, stage)
}

 fun descFromStage(context: Context, stage: Int): String {
    return when (stage) {
        DOWNLOADING_REGIONS -> {
            context.getString(R.string.download_progress_regions)
        }
        DOWNLOADING_AREAS -> {
            context.getString(R.string.download_progress_areas)
        }
        DOWNLOADING_SUCCEEDED -> {
            context.getString(R.string.download_progress_complete)
        }
        else -> {
            context.getString(R.string.download_progress_failed)
        }
    }
}

class DownloadEvent(val status: Int = -1, val progress: Int = 0)