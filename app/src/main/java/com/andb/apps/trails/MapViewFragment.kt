package com.andb.apps.trails

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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
import com.andb.apps.trails.views.GlideApp
import com.andb.apps.trails.xml.MapXMLParser
import com.bumptech.glide.Glide
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.*
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import de.number42.subsampling_pdf_decoder.PDFDecoder
import de.number42.subsampling_pdf_decoder.PDFRegionDecoder
import kotlinx.android.synthetic.main.map_view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main

class MapViewFragment : Fragment() {

    var map: SkiMap? = null
    var mapKey = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapKey = arguments!!.getInt("mapKey")
        Log.d("initialized fragment", "mapKey: $mapKey")
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
        mapLoadingIndicator.visibility = View.VISIBLE
        val handler = Handler()
        Thread(Runnable {
            Log.d("MapViewFragment", "before parsing")
            map = MapXMLParser.parseFull(mapKey)
            Log.d("MapViewFragment", "after parsing")
            handler.post {
                map?.apply {
                    skiMapAreaName?.text = skiArea.name
                    skiMapYear?.text = year.toString()
                    if (!isPdf()) {
                        GlideApp.with(this@MapViewFragment)
                            .asBitmap()
                            .load(imageUrl)
                            .into(object : CustomViewTarget<View, Bitmap>(mapImageView){
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    mapImageView.setImage(ImageSource.cachedBitmap(resource))
                                    mapLoadingIndicator.visibility = View.GONE
                                }

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    mapImageView.recycle()
                                }
                                override fun onResourceCleared(placeholder: Drawable?) {
                                }
                            })

                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            val file = FileDownloader.downloadFile(imageUrl, MapXMLParser.filenameFromURL(imageUrl))
                            withContext(Dispatchers.Main) {
                                mapImageView.apply {
                                    setMinimumTileDpi(120)
                                    setBitmapDecoderFactory { PDFDecoder(0, file, 10f) }
                                    setRegionDecoderFactory { PDFRegionDecoder(0, file, 10f) }
                                    setImage(ImageSource.uri(file.absolutePath))
                                }
                                mapLoadingIndicator.visibility = View.GONE
                            }
                        }

                    }
                }
            }
        }).start()

    }
}