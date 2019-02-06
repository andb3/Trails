package com.andb.apps.trails

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.trails.lists.FavoritesList
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.utils.Utils
import com.andb.apps.trails.xml.AreaXMLParser
import com.andb.apps.trails.xml.MapXMLParser
import com.github.rongi.klaster.Klaster
import com.like.LikeButton
import com.like.OnLikeListener
import com.nostra13.universalimageloader.core.ImageLoader
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
        mapListRecycler.layoutManager = LinearLayoutManager(context)


        loadArea(areaKey)
    }

    fun loadArea(id: Int) {
        activity!!.loadingIndicator.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val recieved = AreaXMLParser.parseFull(id)
            withContext(Dispatchers.Main) {

                skiArea = recieved
                skiArea.apply {
                    activity!!.toolbar.apply {
                        title = name
                        subtitle = ""
                    }
                    Utils.showIfAvailible(liftCount, areaLiftCount, R.string.area_lift_count_text)
                    Utils.showIfAvailible(runCount, areaRunCount, R.string.area_run_count_text)
                    Utils.showIfAvailible(openingYear, areaOpeningYear, R.string.area_opening_year_text)
                    Utils.showIfAvailible(website, areaWebsite, R.string.area_website_text)
                }
                activity!!.loadingIndicator.visibility = View.GONE
                mapAdapter = mapAdapter()
                mapListRecycler.adapter = mapAdapter
                mapAdapter.notifyDataSetChanged()
            }
        }
    }



    fun mapAdapter() = Klaster.get()
        .itemCount { skiArea.maps.size }
        .view(R.layout.map_list_item, layoutInflater)
        .bind { position ->
            val map = skiArea.maps[position]
            if (map.loaded) {
                mapListItemImage.setImageBitmap(ImageLoader.getInstance().loadImageSync(map.imageUrl))
            } else {
                ImageLoader.getInstance().displayImage(map.imageUrl, mapListItemImage)
                map.loaded = true
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