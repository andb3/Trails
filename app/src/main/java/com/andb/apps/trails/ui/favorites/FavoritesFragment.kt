package com.andb.apps.trails.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.andb.apps.dragdropper.DragDropper
import com.andb.apps.dragdropper.dragDropWith
import com.andb.apps.trails.R
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiMap
import com.andb.apps.trails.data.repository.AreasRepository
import com.andb.apps.trails.ui.common.AreaItem
import com.andb.apps.trails.ui.common.EmptyItem
import com.andb.apps.trails.ui.common.MapItem
import com.andb.apps.trails.ui.settings.SettingsFragment
import com.andb.apps.trails.util.dp
import com.andb.apps.trails.util.equalsUnordered
import com.andb.apps.trails.util.mainThread
import com.andb.apps.trails.util.newIoThread
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.favorites_divider.*
import kotlinx.android.synthetic.main.favorites_layout.*
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

const val MAP_DIVIDER_TYPE = 28903
const val AREA_DIVIDER_TYPE = 23890
const val MAP_ITEM_TYPE = 87234
const val AREA_ITEM_TYPE = 98123
const val EMPTY_ITEM_TYPE = 93473

class FavoritesFragment : Fragment() {

    private val favoritesAdapter by lazy { favoritesAdapter() }
    private val viewModel: FavoritesViewModel by viewModel()
    private val areasRepo: AreasRepository by inject()

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
            dragDropWith {
                dragDirection = DragDropper.DIRECTION_BOTH
                constrainDrag { vh ->
                    when (vh.itemViewType) {
                        MAP_DIVIDER_TYPE -> 0..0
                        MAP_ITEM_TYPE -> 1..maps.size
                        AREA_DIVIDER_TYPE -> (maps.size + 1)..(maps.size + 1)
                        else -> (maps.size + 2)..(maps.size + 1 + areas.size)
                    }
                }
                onDropped { oAdapterPos, nAdapterPos ->
                    var oldPos = oAdapterPos - 1
                    var newPos = nAdapterPos - 1
                    if (oldPos in 0 until maps.size) {
                        val draggedMap = maps[oldPos]
                        viewModel.updateMapFavorite(draggedMap, newPos)
                    }

                    oldPos = oldPos - maps.size - 1
                    newPos = newPos - maps.size - 1

                    if (oldPos in 0 until areas.size) {
                        val draggedArea = areas[oldPos]
                        viewModel.updateAreaFavorite(draggedArea, newPos)
                    }
                }
            }
        }

        viewModel.maps.observe(viewLifecycleOwner, Observer { newMaps ->
            val refreshNeeded = !newMaps.equalsUnordered(maps)
            maps.clear()
            maps.addAll(newMaps)
            if (refreshNeeded) {
                refresh()
            }
        })
        viewModel.areas.observe(viewLifecycleOwner, Observer { newAreas ->
            val refreshNeeded = !newAreas.equalsUnordered(areas)
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
        .itemCount { (maps.size + areas.size).coerceAtLeast(1) + 2 /*dividers*/ }
        .view { viewType, parent ->
            when (viewType) {
                MAP_DIVIDER_TYPE, AREA_DIVIDER_TYPE -> layoutInflater.inflate(
                    R.layout.favorites_divider,
                    parent,
                    false
                )
                MAP_ITEM_TYPE -> MapItem(requireContext()).also {
                    it.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                EMPTY_ITEM_TYPE -> EmptyItem(requireContext()).also {
                    it.updatePadding(top = 96.dp)
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
                MAP_DIVIDER_TYPE -> {
                    favoriteDividerSettings.visibility = View.VISIBLE
                    favoriteDividerSettings.setOnClickListener {
                        parentFragmentManager.commit {
                            addToBackStack("settings")
                            val settingsFragment: SettingsFragment = get()
                            add(R.id.settingsHolder, settingsFragment)
                        }
                    }
                    favoriteDividerText.text = getString(R.string.favorites_maps_divider_text)
                }
                AREA_DIVIDER_TYPE -> {
                    favoriteDividerSettings.visibility = View.GONE
                    favoriteDividerText.text = getString(R.string.favorites_area_divider_text)
                }
                MAP_ITEM_TYPE -> {
                    if (adapterPosition >= 0) {
                        val map = maps[adapterPosition - 1 /*divider*/]
                        newIoThread {
                            val area =
                                areasRepo.getAreaByID(map.parentID)//should already be downloaded i.e. instantaneous
                            mainThread {
                                (itemView as MapItem).apply {
                                    setup(map, area?.name ?: "", true)
                                    setOnFavoriteListener { map, favorite ->
                                        viewModel.favoriteMap(map, favorite)
                                    }
                                }
                            }
                        }
                    }

                }
                AREA_ITEM_TYPE -> {
                    val area = areas[adapterPosition - 2 /*dividers*/ - maps.size]
                    (itemView as AreaItem).apply {
                        setup(area)
                        setOnFavoriteListener { area, favorite ->
                            viewModel.favoriteArea(area, favorite)

                        }
                    }
                }
                EMPTY_ITEM_TYPE -> {
                    (itemView as EmptyItem).apply {
                        title = resources.getString(R.string.empty_favorites_title)
                        summary = resources.getString(R.string.empty_favorites_summary)
                        icon = resources.getDrawable(R.drawable.ic_favorite_black_24dp)
                        isButtonVisible = false
                    }
                }
            }
        }
        .getItemViewType { position ->
            if (maps.isEmpty() && areas.isEmpty() && position == 2) {
                return@getItemViewType EMPTY_ITEM_TYPE
            }
            when (position) {
                0 -> MAP_DIVIDER_TYPE
                in 1..maps.size -> MAP_ITEM_TYPE
                maps.size + 1 -> AREA_DIVIDER_TYPE
                else -> AREA_ITEM_TYPE
            }
        }
        .build()


}

