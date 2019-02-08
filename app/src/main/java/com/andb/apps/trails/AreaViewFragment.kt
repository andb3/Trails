package com.andb.apps.trails

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.lists.FavoritesList
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.utils.Utils
import com.andb.apps.trails.utils.dpToPx
import com.andb.apps.trails.xml.AreaXMLParser
import com.andb.apps.trails.xml.MapXMLParser
import com.github.rongi.klaster.Klaster
import com.like.LikeButton
import com.like.OnLikeListener
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.nostra13.universalimageloader.core.process.BitmapProcessor
import io.alterac.blurkit.BlurKit
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.area_view.*
import kotlinx.android.synthetic.main.map_list_item.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main

class AreaViewFragment : Fragment() {

    lateinit var skiArea: SkiArea
    lateinit var mapAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    var areaKey = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        areaKey = arguments!!.getInt("areaKey")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.area_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapListRecycler.layoutManager = GridLayoutManager(context, 2)
        setupFab()

        loadArea(areaKey)
    }

    fun setupFab() {
        areaViewFab.apply {
            imageTintList = ColorStateList.valueOf(Color.WHITE)
            setOnClickListener {
                TransitionManager.beginDelayedTransition(areaLayout, ChangeBounds())
                val translated = areaInfoCard.translationY != 0f
                val translateCard: Float
                val translateFab: Float
                val alpha: Float
                if (!translated) {
                    translateCard = -areaInfoCard.height - dpToPx(8).toFloat()
                    translateFab = -areaInfoCard.height + dpToPx(8).toFloat()
                    alpha = .7f
                    setImageDrawable(resources.getDrawable(R.drawable.ic_expand_more_black_24dp))
                    areaViewDim.visibility = View.VISIBLE
                } else {
                    translateCard = 0f
                    translateFab = 0f
                    alpha = 0f
                    setImageDrawable(resources.getDrawable(R.drawable.ic_info_outline_black_24dp))
                    areaViewDim.visibility = View.GONE
                }
                areaViewDim.animate().alpha(alpha)
                areaInfoCard.animate().translationY(translateCard)
                this.animate().translationY(translateFab)
            }
        }

    }

    fun loadArea(id: Int) {
        activity!!.loadingIndicator.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            skiArea = SkiArea(areasDao().getAreasById(id)[0], ArrayList())
            withContext(Dispatchers.Main) {
                skiArea.apply {
                    areaViewName.text = name
                    Utils.showIfAvailible(liftCount, areaLiftCount, R.string.area_lift_count_text)
                    Utils.showIfAvailible(runCount, areaRunCount, R.string.area_run_count_text)
                    Utils.showIfAvailible(openingYear, areaOpeningYear, R.string.area_opening_year_text)
                    Utils.showIfAvailible(website, areaWebsite, R.string.area_website_text)
                }
                mapAdapter = mapAdapter()
                mapListRecycler.adapter = mapAdapter
            }
            val recieved = AreaXMLParser.parseFull(id, skiArea)
            withContext(Dispatchers.Main) {
                activity!!.loadingIndicator.visibility = View.GONE
                skiArea = recieved
                mapAdapter.notifyDataSetChanged()
            }
        }
    }


    private val options = DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .postProcessor(BitmapProcessor { bitmap ->
            return@BitmapProcessor BlurKit.getInstance().blur(bitmap, 1)
        }).build()

    fun mapAdapter() = Klaster.get()
        .itemCount { skiArea.maps.size }
        .view(R.layout.map_list_item, layoutInflater)
        .bind { position ->
            val map = skiArea.maps[position]
            if (map.loaded) {
                val loadedImage = ImageLoader.getInstance().loadImageSync(map.imageUrl, options)
                mapListItemImage.setImageBitmap(loadedImage)
            } else {
                ImageLoader.getInstance().displayImage(map.imageUrl,
                    mapListItemImage, options, object : SimpleImageLoadingListener() {
                        override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                            //val blurredImage = BlurKit.getInstance().blur(mapListItemImage, 2)
                            //mapListItemImage.setImageBitmap(blurredImage)
                            map.loaded = true
                        }

                    })

            }
            mapListItemYear.text = skiArea.maps[position].year.toString()
            itemView.setOnClickListener {
                val activity = context as FragmentActivity
                val ft = activity.supportFragmentManager.beginTransaction()
                ft.addToBackStack("mapView")

                val fragment = MapViewFragment()
                val bundle = Bundle()
                bundle.putInt("mapKey", skiArea.maps[position].id)
                fragment.arguments = bundle

                ft.add(R.id.mapViewHolder, fragment)
                ft.commit()

            }

            mapListFavoriteButton.apply {
                isLiked = FavoritesList.contains(skiArea.maps[position])
                setOnLikeListener(object : OnLikeListener {
                    override fun liked(p0: LikeButton?) {
                        AsyncTask.execute {
                            val map = MapXMLParser.parseFull(skiArea.maps[position].id, save = true)
                            if (map != null) {
                                FavoritesList.add(map)
                            }
                        }
                    }

                    override fun unLiked(p0: LikeButton?) {
                        AsyncTask.execute {
                            val map = MapXMLParser.parseFull(skiArea.maps[position].id)
                            if (map != null) {
                                FavoritesList.remove(map)
                            }

                        }
                    }
                })
            }
        }
        .build()
}
