package com.andb.apps.trails

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.andb.apps.trails.database.mapsDao
import com.andb.apps.trails.download.FileDownloader
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.MapsRepo
import com.andb.apps.trails.utils.GlideApp
import com.andb.apps.trails.utils.applyEach
import com.andb.apps.trails.utils.mainThread
import com.andb.apps.trails.utils.newIoThread
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.snackbar.Snackbar
import com.like.LikeButton
import com.like.OnLikeListener
import de.number42.subsampling_pdf_decoder.PDFDecoder
import de.number42.subsampling_pdf_decoder.PDFRegionDecoder
import kotlinx.android.synthetic.main.map_view.*
import kotlinx.android.synthetic.main.map_view.view.*
import kotlinx.android.synthetic.main.offline_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapViewFragment : Fragment() {

    private var mapKey = -1
    private var transitionName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapKey = arguments?.getInt("mapKey") ?: -1
        transitionName = arguments?.getString("transitionName") ?: ""
        Log.d("initialized fragment", "mapKey: $mapKey, transitionName: $transitionName")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.map_view, container!!.parent as ViewGroup, false)
        view.mapImageView.transitionName = transitionName
        prepareTransitions(view)

        // Avoid a postponeEnterTransition on orientation change, postpone only if first creation
        if (savedInstanceState == null) {
            postponeEnterTransition()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadMap()

        //startPostponedEnterTransition()


        skiMapAreaName?.text = AreasRepo.getAreaFromMap(mapKey)?.name

        mapViewOfflineItem.apply {
            listOf(offlineTitle, offlineDescription).applyEach {
                setTextColor(Color.WHITE)
            }
            offlineImage.setColorFilter(Color.WHITE)
            offlineRefreshButton.setOnClickListener {
                loadMap()
            }
        }

    }


    private fun loadMap() {
        mapLoadingIndicator.visibility = View.VISIBLE
        listOf(mapViewOfflineItem, mapViewFavorite, mapViewDownload).applyEach { visibility = View.GONE }
        newIoThread {
            val map = MapsRepo.getMapByID(mapKey)
            mainThread {
                map?.apply {
                    skiMapYear?.text = year.toString()
                    if (!isPdf()) {
                        try {
                            GlideApp.with(this@MapViewFragment)
                                .asBitmap()
                                .load(url)
                                .into(object : CustomViewTarget<View, Bitmap>(mapImageView) {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                        mapImageView.setImage(ImageSource.cachedBitmap(resource))
                                        //mapImageView.setImageBitmap(resource)
                                        mapLoadingIndicator.visibility = View.GONE
                                        mapViewDownload.visibility = View.VISIBLE
                                        startPostponedEnterTransition()
                                        Log.d("sharedElementTransition", "starting postponed map view transition")
                                    }

                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                        mapLoadingIndicator.visibility = View.GONE
                                        mapViewOfflineItem.visibility = View.VISIBLE
                                        startPostponedEnterTransition()
                                    }

                                    override fun onResourceCleared(placeholder: Drawable?) {}
                                })
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                            Log.e("glideLoadError", "view is off screen")
                        }
                    } else {
                        newIoThread {
                            val file = FileDownloader.downloadFile(requireContext(), url)
                            mainThread {
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
                                            startPostponedEnterTransition()
                                            Log.d("sharedElementTransition", "starting postponed map view transition")
                                        }

                                        override fun onImageLoadError(e: Exception?) {
                                            super.onImageLoadError(e)
                                            mapLoadingIndicator?.visibility = View.GONE
                                            mapViewOfflineItem.visibility = View.VISIBLE
                                            startPostponedEnterTransition()
                                            Log.d("sharedElementTransition", "starting postponed map view transition")
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

                    mapViewDownload.setOnClickListener {
                        FileDownloader.downloadFileExternal(
                            requireContext(), url, AreasRepo.getAreaFromMap(mapKey)?.name
                                ?: "no_area", year
                        )
                        Snackbar.make(it, "Downloading", Snackbar.LENGTH_SHORT)
                            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
                    }


                }
                if (map == null) {
                    mapLoadingIndicator.visibility = View.GONE
                    mapViewOfflineItem.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun prepareTransitions(rootView: View) {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(R.transition.image_shared_element_transition)
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
                // Map the first shared element name to the ImageView.
                sharedElements[names[0]] = rootView.findViewById(R.id.mapImageView)
            }
        })
    }
}

fun openMapView(id: Int, context: Context, sharedTransitionSource: ImageView) {
    val activity = context as FragmentActivity

    val fragment = MapViewFragment()
    val bundle = Bundle()
    bundle.putInt("mapKey", id)
    bundle.putString("transitionName", sharedTransitionSource.transitionName)
    fragment.arguments = bundle

    activity.supportFragmentManager.commit {
        setReorderingAllowed(true)
        addToBackStack("mapView")
        addSharedElement(sharedTransitionSource, sharedTransitionSource.transitionName)
        //setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        replace(R.id.mapViewHolder, fragment)
    }
}