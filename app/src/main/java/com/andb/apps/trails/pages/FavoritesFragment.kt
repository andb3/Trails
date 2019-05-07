package com.andb.apps.trails.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.andb.apps.trails.R
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.MapsRepo
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

    val favoritesAdapter by lazy { favoritesAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.favorites_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            //TODO: refresh on every favorite change
            withContext(Dispatchers.Main) {
                favoritesAdapter.notifyDataSetChanged()
                favoritesRecycler.scheduleLayoutAnimation()
            }
        }
    }

    private fun favoritesAdapter() = Klaster.get()
        .itemCount { MapsRepo.getFavoriteMaps().size + AreasRepo.getFavoriteAreas().size + 2 }
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
                    val map = MapsRepo.getFavoriteMaps()[adapterPosition + 1]
                    val area = AreasRepo.getAreaById(map.parentId)//should already be downloaded i.e. instantaneous
                    (itemView as MapItem).setup(map, area?.name ?: "", true)
                }
                AREA_ITEM_TYPE -> {
                    val area = AreasRepo.getFavoriteAreas()[adapterPosition + 2]
                    (itemView as AreaItem).setup(area)
                }
            }
        }
        .getItemViewType { position ->
            when (position) {
                0 -> MAP_DIVIDER_TYPE
                in 1..MapsRepo.getFavoriteMaps().size -> MAP_ITEM_TYPE
                MapsRepo.getFavoriteMaps().size + 1 -> AREA_DIVIDER_TYPE
                else -> AREA_ITEM_TYPE
            }
        }
        .build()


}

