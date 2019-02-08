package com.andb.apps.trails.pages

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.trails.AreaViewFragment
import com.andb.apps.trails.GlideApp
import com.andb.apps.trails.MapViewFragment
import com.andb.apps.trails.R
import com.andb.apps.trails.lists.FavoritesList
import com.andb.apps.trails.utils.Utils
import com.andb.apps.trails.xml.AreaXMLParser
import com.bumptech.glide.Glide
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.favorites_area_item.*
import kotlinx.android.synthetic.main.favorites_divider.*
import kotlinx.android.synthetic.main.favorites_layout.*
import kotlinx.android.synthetic.main.favorites_map_item.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main

const val MAP_DIVIDER_TYPE = 28903
const val AREA_DIVIDER_TYPE = 23890
const val MAP_ITEM_TYPE = 87234
const val AREA_ITEM_TYPE = 98123

class FavoritesFragment : Fragment() {

    lateinit var favoritesAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.favorites_layout, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesAdapter = favoritesAdapter()
        favoritesRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = favoritesAdapter
        }


        CoroutineScope(Dispatchers.IO).launch {
            FavoritesList.init()
            withContext(Dispatchers.Main) {
                favoritesAdapter.notifyDataSetChanged()
            }
        }
    }

    fun favoritesAdapter() = Klaster.get()
        .itemCount { FavoritesList.count() }
        .view { viewType, parent ->
            when (viewType) {
                MAP_DIVIDER_TYPE -> layoutInflater.inflate(
                    R.layout.favorites_divider,
                    parent,
                    false
                )
                AREA_DIVIDER_TYPE -> layoutInflater.inflate(
                    R.layout.favorites_divider,
                    parent,
                    false
                )
                MAP_ITEM_TYPE -> layoutInflater.inflate(R.layout.favorites_map_item, parent, false)
                else -> layoutInflater.inflate(R.layout.favorites_area_item, parent, false)
            }

        }
        .bind { fakePos ->
            when (itemViewType) {
                MAP_DIVIDER_TYPE -> favoriteDividerText.text = getString(R.string.favorites_maps_divider_text)
                AREA_DIVIDER_TYPE -> favoriteDividerText.text = getString(R.string.favorites_area_divider_text)
                MAP_ITEM_TYPE -> {
                    val map = FavoritesList.favoriteMaps[FavoritesList.positionInList(adapterPosition)]
                    GlideApp.with(this@FavoritesFragment).load(map.imageUrl)
                        .into(favoritesMapItemImage)
                    favoritesMapItemArea.text = map.skiArea.name
                    favoritesMapItemYear.text = map.year.toString()
                    itemView.setOnClickListener {
                        val activity = context as FragmentActivity
                        val ft = activity.supportFragmentManager.beginTransaction()
                        ft.addToBackStack("mapView")

                        val fragment = MapViewFragment()
                        val bundle = Bundle()
                        bundle.putInt("mapKey", map.id)
                        fragment.arguments = bundle

                        ft.add(R.id.mapViewHolder, fragment)
                        ft.commit()
                    }
                    itemView.setOnLongClickListener {
                        AlertDialog.Builder(context)
                            .setTitle(R.string.favorites_remove_alert_title)
                            .setPositiveButton(
                                R.string.favorites_remove_alert_positive,
                                DialogInterface.OnClickListener { dialog, which ->
                                    FavoritesList.remove(map)
                                    favoritesAdapter.notifyDataSetChanged()
                                })
                            .setNegativeButton(R.string.favorites_remove_alert_negative) { dialog, which ->
                                dialog.cancel()
                            }
                            .show().also {
                                it.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                                    setBackgroundColor(Color.TRANSPARENT)
                                    setTextColor(resources.getColor(R.color.colorAccent))
                                }
                                it.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                                    setBackgroundColor(Color.TRANSPARENT)
                                    setTextColor(resources.getColor(R.color.colorAccent))
                                }
                            }

                        true
                    }

                }
                AREA_ITEM_TYPE -> {
                    val area = FavoritesList.favoriteAreas[FavoritesList.positionInList(adapterPosition)]
                    favoritesAreaItemName.text = area.name
                    Utils.showIfAvailible(area.liftCount, favoritesAreaItemLifts, R.string.area_lift_count_text)
                    Utils.showIfAvailible(area.runCount, favoritesAreaItemRuns, R.string.area_run_count_text)
                    favoritesAreaCurrentMap.setOnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            val mapKey = AreaXMLParser.parseFull(area.id).maps[0].id
                            withContext(Dispatchers.Main) {
                                val activity = context as FragmentActivity
                                val ft = activity.supportFragmentManager.beginTransaction()
                                ft.addToBackStack("mapView")

                                val fragment = MapViewFragment()
                                val bundle = Bundle()
                                bundle.putInt("mapKey", mapKey)
                                fragment.arguments = bundle

                                ft.add(R.id.mapViewHolder, fragment)
                                ft.commit()
                            }
                        }

                    }
                    itemView.setOnClickListener {
                        val fragmentActivity = context as FragmentActivity
                        val ft = fragmentActivity.supportFragmentManager.beginTransaction()

                        val intent = AreaViewFragment()
                        intent.arguments =
                            Bundle().also { it.putInt("areaKey", area.id) }
                        ft.add(R.id.exploreAreaReplacement, intent)
                        ft.addToBackStack("areaView")
                        ft.commit()
                    }

                    itemView.setOnLongClickListener {
                        AlertDialog.Builder(context)
                            .setTitle(R.string.favorites_remove_alert_title)
                            .setPositiveButton(
                                R.string.favorites_remove_alert_positive,
                                DialogInterface.OnClickListener { dialog, which ->
                                    FavoritesList.remove(area)
                                    favoritesAdapter.notifyDataSetChanged()
                                })
                            .setNegativeButton(R.string.favorites_remove_alert_negative) { dialog, which ->
                                dialog.cancel()
                            }
                            .show().also {
                                it.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                                    setBackgroundColor(Color.TRANSPARENT)
                                    setTextColor(resources.getColor(R.color.colorAccent))
                                }
                                it.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                                    setBackgroundColor(Color.TRANSPARENT)
                                    setTextColor(resources.getColor(R.color.colorAccent))
                                }
                            }

                        true
                    }
                }
            }
        }
        .getItemViewType { position ->
            when (position) {
                0 -> MAP_DIVIDER_TYPE
                in 1..FavoritesList.favoriteMaps.size -> MAP_ITEM_TYPE
                FavoritesList.favoriteMaps.size + 1 -> AREA_DIVIDER_TYPE
                else -> AREA_ITEM_TYPE
            }
        }
        .build()


}

