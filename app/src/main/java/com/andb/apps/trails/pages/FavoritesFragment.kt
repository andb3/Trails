package com.andb.apps.trails.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.trails.AreaViewFragment
import com.andb.apps.trails.R
import com.andb.apps.trails.lists.FavoritesList
import com.andb.apps.trails.objects.BaseSkiArea
import com.andb.apps.trails.views.items.AreaItem
import com.andb.apps.trails.views.items.MapItem
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.favorites_divider.*
import kotlinx.android.synthetic.main.favorites_layout.*
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
        val gridLinearManager = GridLayoutManager(context, 2)
        gridLinearManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (favoritesAdapter.getItemViewType(position)) {
                    MAP_ITEM_TYPE -> 1
                    else -> 2
                }
            }
        }
        favoritesRecycler.apply {
            layoutManager = gridLinearManager
            adapter = favoritesAdapter
        }


        CoroutineScope(Dispatchers.IO).launch {
            FavoritesList.init(favoritesAdapter)
            withContext(Dispatchers.Main) {
                favoritesAdapter.notifyDataSetChanged()
                favoritesRecycler.scheduleLayoutAnimation()
            }
        }
    }

    fun favoritesAdapter() = Klaster.get()
        .itemCount { FavoritesList.count() }
        .view { viewType, parent ->
            when (viewType) {
                MAP_DIVIDER_TYPE, AREA_DIVIDER_TYPE -> layoutInflater.inflate(
                    R.layout.favorites_divider,
                    parent,
                    false
                )
                MAP_ITEM_TYPE -> MapItem(context ?: this.requireContext()).also {
                    it.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                else -> AreaItem(context ?: this.requireContext()).also {
                    it.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            }

        }
        .bind { fakePos ->
            when (itemViewType) {
                MAP_DIVIDER_TYPE -> favoriteDividerText.text = getString(R.string.favorites_maps_divider_text)
                AREA_DIVIDER_TYPE -> favoriteDividerText.text = getString(R.string.favorites_area_divider_text)
                MAP_ITEM_TYPE -> {
                    val map = FavoritesList.favoriteMaps[FavoritesList.positionInList(adapterPosition)]
                    (itemView as MapItem).setup(map, true)
                }
                AREA_ITEM_TYPE -> {
                    val area = FavoritesList.favoriteAreas[FavoritesList.positionInList(adapterPosition)]
                    (itemView as AreaItem).setup(area)
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

