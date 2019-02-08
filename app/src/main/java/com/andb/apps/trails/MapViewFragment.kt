package com.andb.apps.trails

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument
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
import com.davemorrissey.labs.subscaleview.ImageSource
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
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
                        mapImageView?.apply {
                            val image = ImageLoader.getInstance().loadImage(imageUrl, object : SimpleImageLoadingListener() {
                                    override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                                        if(loadedImage!=null) {
                                            mapLoadingIndicator.visibility = View.GONE
                                            mapImageView.setImage(ImageSource.bitmap(loadedImage))
                                        }
                                    }
                                })
                            //setColorFilter(Color.argb(0, 0, 0, 0))
                        }
                    }else{
                        CoroutineScope(Dispatchers.IO).launch {
                            val file = FileDownloader.downloadFile(imageUrl, MapXMLParser.filenameFromURL(imageUrl))
                            withContext(Dispatchers.Main){
                                mapImageView.apply {
                                    setMinimumTileDpi(120)
                                    setBitmapDecoderFactory{PDFDecoder(0, file, 10f)}
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