package com.andb.apps.trails.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.andb.apps.trails.R
import com.andb.apps.trails.data.local.Prefs
import com.andb.apps.trails.data.model.SkiRegionTree
import com.andb.apps.trails.data.remote.Updater
import com.andb.apps.trails.ui.area.openAreaView
import com.andb.apps.trails.util.newIoThread
import kotlinx.android.synthetic.main.explore_layout.*
import org.koin.android.viewmodel.ext.android.viewModel


class ExploreFragment : Fragment() {

    private val exploreAdapter = ExploreAdapter()
    val viewModel: ExploreViewModel by viewModel()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.explore_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exploreRegionRecycler.layoutManager = LinearLayoutManager(context)
        exploreRegionRecycler.adapter = exploreAdapter
        exploreRegionRecycler.setHasFixedSize(true)

        exploreAdapter.setOnRegionClickListener { viewModel.nextRegion(it.id) }
        exploreAdapter.setOnAreaClickListener { openAreaView(it.id, requireContext()) }
        exploreAdapter.setOnAreaFavoriteListener { area, favorite -> viewModel.updateFavorite(area, favorite) }
        exploreAdapter.setOnHeaderSwitchListener { viewModel.setBaseRegion(it) }
        exploreAdapter.setOnOfflineRefreshListener {
            newIoThread {
                Updater.updateRegions()
                Updater.updateAreas()
                viewModel.refresh()
            }
        }

        if (viewModel.isFirstLoad()) {
            viewModel.setup(Prefs.startingRegion)
        }

        viewModel.tree.observe(viewLifecycleOwner, onTreeChangeListener)
        viewModel.loading.observe(viewLifecycleOwner, onLoadingChangeListener)
        viewModel.offline.observe(viewLifecycleOwner, onOfflineChangeListener)

    }

    private val onTreeChangeListener = Observer<SkiRegionTree> { region ->
        //Log.d("onTreeChangeListener", "new parent: $region")
        exploreAdapter.updateTree(region)
    }



    private val onOfflineChangeListener = Observer<Boolean> { offline ->
        if (offline) {
            exploreAdapter.notifyItemInserted(exploreAdapter.itemCount - 1)
        } else {
            exploreAdapter.notifyItemRemoved(exploreAdapter.itemCount)
        }
    }

    private val onLoadingChangeListener = Observer<Boolean> { loading ->
        if (loading) {
            exploreLoadingIndicator.visibility = View.VISIBLE
        } else {
            exploreLoadingIndicator.visibility = View.GONE
        }
    }


    /*private fun exploreAdapter() = Klaster.get()
        .itemCount {
            (parentRegionTree?.childrenSize() ?: 0) + 1 + (if (offline) 1 else 0)
        }
        .view { viewType, parent ->
            when (viewType) {
                EXPLORE_REGION_ITEM_TYPE -> layoutInflater.inflate(R.layout.region_item, parent, false)
                EXPLORE_AREA_ITEM_TYPE -> AreaItem(context ?: this.requireContext()).also {
                    it.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                EXPLORE_HEADER_ITEM_TYPE -> layoutInflater.inflate(R.layout.explore_header, parent, false)
                else -> layoutInflater.inflate(R.layout.offline_item, parent, false)
            }
        }
        .bind { position ->
            when (itemViewType) {
                EXPLORE_REGION_ITEM_TYPE -> {
                    parentRegionTree?.apply {
                        val region = childRegions[adapterPosition - 1]

                        regionName.text = region.name
                        regionMaps.showIfAvailable(region.mapCount, R.string.map_count)
                        itemView.setOnClickListener {
                            //viewModel.nextRegion(region)
                            nextRegion(region.id)
                        }

                        regionChildrenChip1.visibility = if ((region.childRegions.size + region.childAreas.size) > 0) View.VISIBLE else View.GONE
                        regionChildrenChip2.visibility = if ((region.childRegions.size + region.childAreas.size) > 1) View.VISIBLE else View.GONE



                        (regionChildrenChip1 and regionChildrenChip2).applyEach {
                            text = ""
                            val progressDrawable = CircularProgressDrawable(requireContext()).apply {
                                setColorSchemeColors(resources.getColor(R.color.colorAccent))
                                strokeWidth = 4f
                                start()
                            }
                            setIconOnly(progressDrawable)
                        }


                        val items = region.childRegions.sortedByDescending { it.mapCount }.map { ChipItem(region.id, region = it) } + region.childAreas.sortedByDescending { it.maps.size }.map { ChipItem(region.id, area = it) }
                        regionChildrenChip1.setupChild(items.getOrNull(0))
                        regionChildrenChip2.setupChild(items.getOrNull(1))
                    }
                }
                EXPLORE_AREA_ITEM_TYPE -> {
                    parentRegionTree?.apply {
                        val area = childAreas[position - 1]
                        (itemView as AreaItem).setup(area) { favorite ->
                            area.favorite = favorite
                            newIoThread {
                                AreasRepo.areasDao.updateArea(area)
                            }
                        }
                    }
                }
                EXPLORE_HEADER_ITEM_TYPE -> {
                    exploreHeaderRegionName.text = if (!parentRegionTree?.name.isNullOrEmpty()) parentRegionTree!!.name else getNameIfBase(viewModel.baseRegionOffline)
                    switchRegionButton.apply {
                        visibility = if (viewModel.regionStack.size == 1) View.VISIBLE else View.GONE
                        setOnClickListener {
                            val popup = PopupMenu(context, switchRegionButton)
                            popup.menuInflater.inflate(R.menu.region_switcher, popup.menu)
                            popup.show()
                            popup.setOnMenuItemClickListener {
                                when (it.itemId) {
                                    R.id.region_americas -> viewModel.setBaseRegion(1)
                                    R.id.region_europe -> viewModel.setBaseRegion(2)
                                    R.id.region_asia -> viewModel.setBaseRegion(3)
                                    R.id.region_oceania -> viewModel.setBaseRegion(4)
                                    else -> return@setOnMenuItemClickListener false
                                }

                                return@setOnMenuItemClickListener true
                            }
                        }
                    }
                }
                EXPLORE_OFFLINE_ITEM_TYPE -> {
                    offlineRefreshButton.setOnClickListener {
                        viewModel.setBaseRegion(viewModel.baseRegionOffline)
                    }
                }
            }

        }.getItemViewType { pos ->
            when {
                pos == 0 -> EXPLORE_HEADER_ITEM_TYPE
                1<=pos && pos <= parentRegionTree?.childrenSize() ?: 0 -> if(parentRegionTree!!.childRegions.isEmpty()) EXPLORE_AREA_ITEM_TYPE else EXPLORE_REGION_ITEM_TYPE
                else->EXPLORE_OFFLINE_ITEM_TYPE
            }
        }
        .build()


    fun nextRegion(regionID: Int) {
        viewModel.nextRegion(regionID)
        exploreAdapter.notifyDataSetChanged()
        exploreRegionRecycler.scrollToPosition(0)
        exploreRegionRecycler.scheduleLayoutAnimation()
    }

    private fun Chip.setupChild(chipItem: ChipItem?) {

        when {
            chipItem == null -> visibility = View.GONE
            chipItem.area != null -> {
                visibility = View.VISIBLE
                text = chipItem.area.name
                setTextOnly()
                setOnClickListener {
                    openAreaView(chipItem.area.id, context)
                }
            }
            chipItem.region != null -> {
                visibility = View.VISIBLE
                text = chipItem.region.name
                setTextOnly()
                setOnClickListener {
                    nextRegion(chipItem.region.id)
                }
            }
            else -> {
                visibility = View.VISIBLE
                text = ""
                setIconOnly(resources.getDrawable(R.drawable.ic_cloud_off_black_24dp))
            }
        }

    }*/

}

