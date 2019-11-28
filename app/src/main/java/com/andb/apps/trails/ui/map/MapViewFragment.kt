package com.andb.apps.trails.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import com.andb.apps.trails.R
import com.andb.apps.trails.data.model.isPdf
import com.andb.apps.trails.util.*
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
import kotlinx.android.synthetic.main.offline_item.view.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File

class MapViewFragment : Fragment() {

    private var mapKey = -1
    private var transitionName = ""

    private val fileDownloader: FileDownloader by inject()

    val viewModel: MapViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapKey = arguments?.getInt("mapKey") ?: -1
        transitionName = arguments?.getString("transitionName") ?: ""
        Log.d("initialized fragment", "mapKey: $mapKey, transitionName: $transitionName")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.map_view, container!!.parent as ViewGroup, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //startPostponedEnterTransition()

        setLoading()
        viewModel.setMap(mapKey)

        viewModel.areaName.observe(viewLifecycleOwner, areaNameObserver)
        viewModel.year.observe(viewLifecycleOwner, yearObserver)
        viewModel.favorite.observe(viewLifecycleOwner, favoriteObserver)
        viewModel.imageURL.observe(viewLifecycleOwner, imageURLObserver)

        mapViewOfflineItem.apply {
            listOf(offlineTitle, offlineDescription).applyEach {
                setTextColor(Color.WHITE)
            }
            offlineImage.setColorFilter(Color.WHITE)
            offlineRefreshButton.setOnClickListener {
                viewModel.setMap(mapKey)
            }
        }

        mapViewDownload.setOnClickListener { clickedView ->
            viewModel.downloadCurrent {
                Snackbar.make(clickedView, "Downloading", Snackbar.LENGTH_SHORT)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
            }
        }

        mapViewFavorite.setOnLikeListener(object : OnLikeListener {
            override fun liked(likeButton: LikeButton?) {
                viewModel.favoriteMap(true)
            }

            override fun unLiked(likeButton: LikeButton?) {
                viewModel.favoriteMap(false)
            }

        })

    }

    private val areaNameObserver = Observer<String> { areaName ->
        skiMapAreaName.text = areaName
    }

    private val yearObserver = Observer<Int> { year ->
        skiMapYear.text = year.toString()
    }

    private val favoriteObserver = Observer<Boolean> { favorite ->
        //only called at initial skiMap set, after controlled by LikeView
        mapViewFavorite.isLiked = favorite
    }

    private val imageURLObserver = Observer<String> { url ->
        setLoading()
        if (url.isPdf()) {
            newIoThread {
                val file: File = fileDownloader.downloadFile(url)
                mainThread {
                    setImagePDF(file)
                }
            }
        } else {
            setImageStandard(url)
        }
    }


    private fun setLoading() {
        mapViewOfflineItem.visibility = View.GONE
        mapLoadingIndicator.visibility = View.VISIBLE
        mapViewDownload.visibility = View.GONE
    }

    private fun setOffline() {
        mapViewOfflineItem.visibility = View.VISIBLE
        mapLoadingIndicator.visibility = View.GONE
        mapViewDownload.visibility = View.GONE
    }

    private fun setLoaded() {
        mapViewOfflineItem.visibility = View.GONE
        mapLoadingIndicator.visibility = View.GONE
        mapViewDownload.visibility = View.VISIBLE
    }

    fun setImagePDF(file: File) {
        mapImageView?.apply {
            setMinimumTileDpi(120)
            setBitmapDecoderFactory { PDFDecoder(0, file, 10f) }
            setRegionDecoderFactory { PDFRegionDecoder(0, file, 10f) }
            setImage(ImageSource.uri(file.absolutePath))
            setOnImageEventListener(object :
                SubsamplingScaleImageView.DefaultOnImageEventListener() {
                override fun onReady() {
                    super.onReady()
                    setLoaded()
                }

                override fun onImageLoadError(e: Exception?) {
                    super.onImageLoadError(e)
                    setOffline()
                }
            })
        }
    }

    fun setImageStandard(url: String) {
        GlideApp.with(this@MapViewFragment)
            .asBitmap()
            .load(url)
            .into(object : CustomViewTarget<View, Bitmap>(mapImageView) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    mapImageView.setImage(ImageSource.cachedBitmap(resource))
                    setLoaded()
                    startPostponedEnterTransition()
                    Log.d("sharedElementTransition", "starting postponed map view transition")
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    setOffline()
                    startPostponedEnterTransition()
                }

                override fun onResourceCleared(placeholder: Drawable?) {}
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
        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        replace(R.id.mapViewHolder, fragment)
    }
}