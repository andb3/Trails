package com.andb.apps.trails

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.andb.apps.trails.download.PARENT_REGIONS
import com.andb.apps.trails.download.setupRegions
import com.andb.apps.trails.download.updateAreas
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
const val DOWNLOADING_SUCEEDED = 2
const val DOWNLOADING_FAILED = 3


class InitialDownloadService : Service() {
    var maxItems: Int = PARENT_REGIONS
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

    suspend fun startDownload() {
        createNotification()
        val regionsDownloaded = if(!Once.beenDone(TAG_REGION_SETUP)) downloadRegions() else true
        if (regionsDownloaded) {

            Once.markDone(TAG_REGION_SETUP)
            val areasDownloaded = if(!Once.beenDone(TAG_AREA_SETUP)) downloadAreas() else true

            if (areasDownloaded) {
                Once.markDone(TAG_AREA_SETUP)
                updateStage(3)
            }else{
                updateStage(4)
            }

        } else {
            updateStage(4)
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
    fun createNotification() {

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


        val notification = notificationBuilder.build()
        updateNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    fun updateNotification() {
        notifManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    fun updateProgress(amount: Int = 1) {
        itemCount += amount
        notificationBuilder.setProgress(maxItems, itemCount, false)
        updateNotification()
        EventBus.getDefault().post(DownloadEvent(progress = progress()))
    }

    private fun progress(): Int = (itemCount.toFloat() / maxItems).toInt() * 100


    fun nextStage(items: Int) {
        stage++
        maxItems = items
        updateStage()
    }

    fun updateStage(stage: Int = this.stage) {
        this.stage = stage
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
        DOWNLOADING_SUCEEDED -> {
            context.getString(R.string.download_progress_complete)
        }
        else -> {
            context.getString(R.string.download_progress_failed)
        }
    }
}

class DownloadEvent(val status: Int = -1, val progress: Int = 0)