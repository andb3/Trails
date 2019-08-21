package com.andb.apps.trails.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.andb.apps.trails.FavoritesViewModel
import com.andb.apps.trails.R
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.MapsRepo
import com.andb.apps.trails.utils.mainThread
import com.andb.apps.trails.utils.newIoThread
import com.andb.apps.trails.views.AreaItem
import com.andb.apps.trails.views.MapItem
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.favorites_divider.*
import kotlinx.android.synthetic.main.favorites_layout.*
import kotlinx.coroutines.*

const val MAP_DIVIDER_TYPE = 28903
const val AREA_DIVIDER_TYPE = 23890
const val MAP_ITEM_TYPE = 87234
const val AREA_ITEM_TYPE = 98123

class FavoritesFragment : Fragment() {

    val favoritesAdapter by lazy { favoritesAdapter() }
    private val viewModel: FavoritesViewModel by lazy {
        ViewModelProviders.of(this).get(FavoritesViewModel::class.java)
    }

    val maps = ArrayList<SkiMap>()
    val areas = ArrayList<SkiArea>()

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

            //map items only span one grid position, all others span 2 aka full width
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

        viewModel.getFavoriteMaps().observe(viewLifecycleOwner, Observer { newMaps ->
            val refreshNeeded = newMaps != maps
            maps.clear()
            maps.addAll(newMaps)
            if (refreshNeeded) {
                refresh()
            }
        })
        viewModel.getFavoriteAreas().observe(viewLifecycleOwner, Observer { newAreas ->
            val refreshNeeded = newAreas != areas
            areas.clear()
            areas.addAll(newAreas)
            if (refreshNeeded) {
                refresh()
            }
        })
    }

    fun refresh(animate: Boolean = true) {
        favoritesAdapter.notifyDataSetChanged()
        if (animate) {
            favoritesRecycler.scheduleLayoutAnimation()
        }
    }


    private fun favoritesAdapter() = Klaster.get()
        .itemCount { maps.size + areas.size + 2 /*dividers*/ }
        .view { viewType, parent ->
            when (viewType) {
                MAP_DIVIDER_TYPE, AREA_DIVIDER_TYPE -> layoutInflater.inflate(
                    R.layout.favorites_divider,
                    parent,
                    false
                )
                MAP_ITEM_TYPE -> MapItem(
                    context ?: this.requireContext()
                ).also {
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
        .bind { _ ->
            when (itemViewType) {
                MAP_DIVIDER_TYPE -> favoriteDividerText.text = getString(R.string.favorites_maps_divider_text)
                AREA_DIVIDER_TYPE -> favoriteDividerText.text = getString(R.string.favorites_area_divider_text)
                MAP_ITEM_TYPE -> {
                    if(adapterPosition>=0) {
                        val map = maps[adapterPosition - 1 /*divider*/]
                        newIoThread {
                            val area = AreasRepo.getAreaById(map.parentId)//should already be downloaded i.e. instantaneous
                            mainThread {
                                (itemView as MapItem).setup(map, area?.name ?: "", true)
                            }
                        }
                    }

                }
                AREA_ITEM_TYPE -> {
                    val area = areas[adapterPosition - 2 /*dividers*/ - maps.size]
                    (itemView as AreaItem).setup(area)
                }
            }
        }
        .getItemViewType { position ->
            when (position) {
                0 -> MAP_DIVIDER_TYPE
                in 1..maps.size -> MAP_ITEM_TYPE
                maps.size + 1 -> AREA_DIVIDER_TYPE
                else -> AREA_ITEM_TYPE
            }
        }
        .build()


}

