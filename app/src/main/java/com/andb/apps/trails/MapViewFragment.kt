package com.andb.apps.trails

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.andb.apps.trails.download.FileDownloader
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.MapsRepo
import com.andb.apps.trails.utils.ioThread
import com.andb.apps.trails.utils.mainThread
import com.andb.apps.trails.utils.newIoThread
import com.andb.apps.trails.views.GlideApp
import com.andb.apps.trails.xml.filenameFromURL
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import de.number42.subsampling_pdf_decoder.PDFDecoder
import de.number42.subsampling_pdf_decoder.PDFRegionDecoder
import kotlinx.android.synthetic.main.map_view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main

class MapViewFragment : Fragment() {

    private var mapKey = -1
    private var areaName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapKey = arguments?.getInt("mapKey") ?: -1
        areaName = arguments?.getString("areaName") ?: ""
        Log.d("initialized fragment", "mapKey: $mapKey")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.map_view, container!!.parent as ViewGroup, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setStatusBarColors(activity!!, false)
        mapLoadingIndicator.visibility = View.VISIBLE
        newIoThread {
            val map = MapsRepo.getMapById(mapKey)

            mainThread {

                map?.apply{
                    skiMapAreaName?.text = areaName
                    skiMapYear?.text = year.toString()
                    if (!isPdf()) {
                        try {
                            GlideApp.with(this@MapViewFragment)
                                .asBitmap()
                                .load(url)
                                .into(object : CustomViewTarget<View, Bitmap>(mapImageView) {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                        mapImageView.setImage(ImageSource.cachedBitmap(resource))
                                        mapLoadingIndicator.visibility = View.GONE
                                    }

                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                        skiMapAreaName?.text = resources.getText(R.string.offline_error_title)
                                        skiMapYear?.text = resources.getText(R.string.offline_error_desc)
                                        mapLoadingIndicator.visibility = View.GONE

                                        val drawable = resources.getDrawable(R.drawable.ill_connection)
                                        mapImageView.setImage(ImageSource.bitmap(drawable.toBitmap()))
                                        mapImageView.isZoomEnabled = false
                                    }

                                    override fun onResourceCleared(placeholder: Drawable?) {}
                                })
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                            Log.e("glideLoadError", "view is off screen")
                        }


                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            val file = FileDownloader.downloadFile(url, filenameFromURL(url))
                            withContext(Dispatchers.Main) {
                                mapImageView?.apply {
                                    setMinimumTileDpi(120)
                                    setBitmapDecoderFactory { PDFDecoder(0, file, 10f) }
                                    setRegionDecoderFactory { PDFRegionDecoder(0, file, 10f) }
                                    setImage(ImageSource.uri(file.absolutePath))
                                }
                                mapLoadingIndicator?.visibility = View.GONE
                            }
                        }

                    }
                }
                if(map==null){
                    skiMapAreaName?.text = resources.getText(R.string.offline_error_title)
                    skiMapYear?.text = resources.getText(R.string.offline_error_desc)
                    mapLoadingIndicator.visibility = View.GONE

                    val drawable = resources.getDrawable(R.drawable.ill_connection)
                    mapImageView.setImage(ImageSource.bitmap(drawable.toBitmap()))
                    mapImageView.isZoomEnabled = false
                }
            }
        }

    }
}

fun setStatusBarColors(activity: Activity, light: Boolean = true) {
    val color = if (light) Color.WHITE else Color.BLACK
    activity.window.statusBarColor = color
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        activity.window.decorView.systemUiVisibility = if (light) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else 0
    }
}

fun openMapView(id: Int, areaName: String, context: Context) {
    val activity = context as FragmentActivity
    val ft = activity.supportFragmentManager.beginTransaction()
    ft.addToBackStack("mapView")
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)

    val fragment = MapViewFragment()
    val bundle = Bundle()
    bundle.putInt("mapKey", id)
    Log.d("openMapView", "areaName: $areaName")
    bundle.putString("areaName", areaName)
    fragment.arguments = bundle

    ft.add(R.id.mapViewHolder, fragment)
    ft.commit()
}
