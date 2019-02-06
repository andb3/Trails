package com.andb.apps.trails

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.andb.apps.trails.download.FileDownloader
import com.andb.apps.trails.lists.FavoritesList
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.xml.MapXMLParser
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import kotlinx.android.synthetic.main.map_view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main

class MapViewFragment : Fragment() {

    var map: SkiMap? = null
    var mapKey = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapKey = arguments!!.getInt("mapKey")
        Log.d("inititalized fragment", "mapKey: $mapKey")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.map_view, container!!.parent as ViewGroup, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("MapViewFragment", "before thread")
        mapPdfView.setBackgroundColor(Color.BLACK)
        mapLoadingIndicator.visibility = View.VISIBLE
        val handler = Handler()
        Thread(Runnable {
            Log.d("MapViewFragment", "before parsing")
            map = MapXMLParser.parseFull(mapKey, save = FavoritesList.contains(mapKey))
            Log.d("MapViewFragment", "after parsing")
            handler.post {
                map?.apply {
                    skiMapAreaName?.text = skiArea.name
                    skiMapYear?.text = year.toString()
                    if (!isPdf()) {
                        skiMapImage?.apply {
                            ImageLoader.getInstance().displayImage(
                                imageUrl,
                                this,
                                object : SimpleImageLoadingListener() {
                                    override fun onLoadingComplete(
                                        imageUri: String?,
                                        view: View?,
                                        loadedImage: Bitmap?
                                    ) {
                                        mapLoadingIndicator.visibility = View.GONE
                                        mapPdfView.visibility = View.GONE
                                    }
                                })
                            setColorFilter(Color.argb(0, 0, 0, 0))
                        }
                    }else{
                        CoroutineScope(Dispatchers.IO).launch {
                            val file = FileDownloader.downloadFile(imageUrl, MapXMLParser.filenameFromURL(imageUrl))
                            withContext(Dispatchers.Main){
                                mapPdfView.fromFile(file).pageFitPolicy(FitPolicy.BOTH).load()
                                skiMapImage.visibility = View.GONE
                                mapLoadingIndicator.visibility = View.GONE
                            }
                        }

                    }
                }
            }
        }).start()

    }
}