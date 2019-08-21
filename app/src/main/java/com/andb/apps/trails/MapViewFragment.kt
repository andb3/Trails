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
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.andb.apps.trails.database.mapsDao
import com.andb.apps.trails.download.FileDownloader
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.MapsRepo
import com.andb.apps.trails.utils.*
import com.andb.apps.trails.views.GlideApp
import com.andb.apps.trails.xml.filenameFromURL
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.snackbar.Snackbar
import com.like.LikeButton
import com.like.OnLikeListener
import de.number42.subsampling_pdf_decoder.PDFDecoder
import de.number42.subsampling_pdf_decoder.PDFRegionDecoder
import kotlinx.android.synthetic.main.map_view.*
import kotlinx.android.synthetic.main.map_view.mapLoadingIndicator
import kotlinx.android.synthetic.main.map_view.mapViewOfflineItem
import kotlinx.android.synthetic.main.map_view.view.*
import kotlinx.android.synthetic.main.offline_item.view.*
import kotlinx.coroutines.*
import java.lang.Exception

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
        loadMap()


        mapViewOfflineItem.apply {
            listOf(offlineTitle, offlineDescription).applyEach {
                setTextColor(Color.WHITE)
            }
            offlineImage.setColorFilter(Color.WHITE)
            offlineRefreshButton.setOnClickListener {
                loadMap()
            }
        }

        mapViewDownload.setOnClickListener {
            Snackbar.make(it, "Downloading to external storage is coming soon", Snackbar.LENGTH_SHORT)
                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
        }

    }


    private fun loadMap() {
        mapLoadingIndicator.visibility = View.VISIBLE
        listOf(mapViewOfflineItem, mapViewFavorite, mapViewDownload).applyEach { visibility = View.GONE }
        newIoThread {
            val map = MapsRepo.getMapById(mapKey)

            mainThread {

                map?.apply {
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
                                        mapViewDownload.visibility = View.VISIBLE
                                    }

                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                        mapLoadingIndicator.visibility = View.GONE
                                        mapViewOfflineItem.visibility = View.VISIBLE
                                    }

                                    override fun onResourceCleared(placeholder: Drawable?) {}
                                })
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                            Log.e("glideLoadError", "view is off screen")
                        }


                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            val file = FileDownloader.downloadFile(requireContext(), url, filenameFromURL(url))
                            withContext(Dispatchers.Main) {
                                mapImageView?.apply {
                                    setMinimumTileDpi(120)
                                    setBitmapDecoderFactory { PDFDecoder(0, file, 10f) }
                                    setRegionDecoderFactory { PDFRegionDecoder(0, file, 10f) }
                                    setImage(ImageSource.uri(file.absolutePath))
                                    setOnImageEventListener(object :
                                        SubsamplingScaleImageView.DefaultOnImageEventListener() {
                                        override fun onReady() {
                                            super.onReady()
                                            this@MapViewFragment.mapLoadingIndicator?.visibility = View.GONE
                                            this@MapViewFragment.mapViewDownload.visibility = View.VISIBLE

                                        }

                                        override fun onImageLoadError(e: Exception?) {
                                            super.onImageLoadError(e)
                                            mapLoadingIndicator?.visibility = View.GONE
                                            mapViewOfflineItem.visibility = View.VISIBLE
                                        }
                                    })
                                }
                            }
                        }
                    }

                    mapViewFavorite.apply {
                        isLiked = map.favorite
                        visibility = View.VISIBLE
                        setOnLikeListener(object : OnLikeListener {
                            override fun liked(likeButton: LikeButton?) {
                                map.favorite = true
                                newIoThread {
                                    mapsDao().updateMap(map)
                                }
                            }

                            override fun unLiked(likeButton: LikeButton?) {
                                map.favorite = false
                                newIoThread {
                                    mapsDao().updateMap(map)
                                }
                            }
                        })
                    }


                }
                if (map == null) {
                    mapLoadingIndicator.visibility = View.GONE
                    mapViewOfflineItem.visibility = View.VISIBLE
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
