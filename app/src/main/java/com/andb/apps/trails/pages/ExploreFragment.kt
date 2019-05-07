package com.andb.apps.trails.pages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.andb.apps.trails.ChipItem
import com.andb.apps.trails.R
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.openAreaView
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.RegionsRepo
import com.andb.apps.trails.utils.*
import com.andb.apps.trails.views.items.AreaItem
import com.github.rongi.klaster.Klaster
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.explore_header.*
import kotlinx.android.synthetic.main.explore_layout.*
import kotlinx.android.synthetic.main.offline_item.view.*
import kotlinx.android.synthetic.main.region_item.*

const val EXPLORE_HEADER_ITEM_TYPE = 98342
const val EXPLORE_AREA_ITEM_TYPE = 32940
const val EXPLORE_REGION_ITEM_TYPE = 34987

class ExploreFragment : Fragment() {

    val exploreAdapter by lazy { exploreAdapter() }
    val regionStack = ArrayList<SkiRegion>()
    var childRegions = listOf<SkiRegion>()
    var childAreas = listOf<SkiArea>()
    private val chips = ArrayList<ChipItem>()


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

        setParentRegion(1, true)
    }


    private fun setParentRegion(id: Int, addToStack: Boolean = false) {
        setOnline()
        newIoThread {
            val region = RegionsRepo.getRegionById(id)
            if (region != null) {
                if (addToStack) {
                    regionStack.add(region)
                } else {
                    regionStack[0] = region
                }
                refreshRegions(region)
                mainThread {
                    exploreAdapter.notifyDataSetChanged()
                    exploreRegionRecycler.scrollToPosition(0)
                    exploreRegionRecycler.scheduleLayoutAnimation()
                    exploreLoadingIndicator.visibility = View.GONE
                }
            } else {
                mainThread {
                    setOffline {
                        setParentRegion(id, addToStack)
                    }
                }

            }

        }
    }

    fun setOffline(onRefresh: ((View) -> Unit)? = null) {
        exploreLoadingIndicator.visibility = View.GONE
        exploreOfflineItem.visibility = View.VISIBLE
        exploreOfflineItem.offlineRefreshButton.setOnClickListener(onRefresh)
    }

    fun setOnline() {
        exploreLoadingIndicator.visibility = View.VISIBLE
        exploreOfflineItem.visibility = View.GONE
    }

    fun isOnline(): Boolean{
        return regionStack.last().childIds == childRegions.map { it.id } && regionStack.last().areaIds == childAreas.map { it.id }
    }

    private fun exploreAdapter() = Klaster.get()
        .itemCount {
            when {
                regionStack.isEmpty() -> 0
                childRegions.isEmpty() -> childAreas.size + 1
                else -> childRegions.filter { it.mapCount != 0 }.size + 1
            }
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
                else -> layoutInflater.inflate(R.layout.explore_header, parent, false)
            }
        }
        .bind { position ->
            when (itemViewType) {
                EXPLORE_REGION_ITEM_TYPE -> {
                    val region = childRegions.filter { it.mapCount != 0 }[position - 1]

                    regionName.text = region.name
                    regionMaps.showIfAvailable(region.mapCount, R.string.map_count)
                    itemView.setOnClickListener {
                        nextRegion(region)
                    }
                    val items = chips.filter { it.parentId == region.id }
                    if (items.isNotEmpty()) {
                        regionChildrenChip1.setupChild(items.getOrNull(0))
                        regionChildrenChip2.setupChild(items.getOrNull(1))
                    } else {

                        fun createLoadingDrawable(): CircularProgressDrawable {
                            val progressDrawable = CircularProgressDrawable(requireContext())
                            progressDrawable.apply {
                                setColorSchemeColors(resources.getColor(R.color.colorAccent))
                                strokeWidth = 4f
                                start()
                            }
                            return progressDrawable
                        }

                        listOf(regionChildrenChip1, regionChildrenChip2).applyEach {
                            visibility = View.VISIBLE
                            chipIcon = createLoadingDrawable()
                            text = ""
                            setIcon()
                        }

                        newIoThread {

                            val regionChildren = RegionsRepo.getRegions(region)
                                .sortedByDescending { it.mapCount }
                            val regionAreas = AreasRepo.getAreasByParent(region)
                                .sortedByDescending { it.maps.size }

                            if (regionChildren.isNotEmpty()) {
                                chips.addAll(regionChildren.take(2).map { ChipItem(region.id, region = it) })
                            } else {
                                chips.addAll(regionAreas.take(2).map { ChipItem(region.id, area = it) })
                            }

                            val online = region.childIds.equalsUnordered(regionChildren.map { it.id }) && region.areaIds.equalsUnordered(regionAreas.map { it.id })

                            mainThread {
                                if(online) {
                                    val newItems = chips.filter { it.parentId == region.id }
                                    regionChildrenChip1.apply {
                                        chipIcon = null
                                        setupChild(newItems.getOrNull(0))
                                        setText()
                                    }
                                    regionChildrenChip2.apply {
                                        chipIcon = null
                                        setupChild(newItems.getOrNull(1))
                                        setText()
                                    }
                                }else{
                                    listOf(regionChildrenChip1, regionChildrenChip2).applyEach {
                                        chipIcon = resources.getDrawable(R.drawable.ic_cloud_off_black_24dp)
                                    }
                                }
                            }
                        }
                    }


                }
                EXPLORE_AREA_ITEM_TYPE -> {
                    (itemView as AreaItem).setup(childAreas[position - 1])
                }
                EXPLORE_HEADER_ITEM_TYPE -> {
                    exploreHeaderRegionName.text = regionStack.lastOrNull()?.name
                    switchRegionButton.apply {
                        visibility = if (regionStack.size == 1) View.VISIBLE else View.GONE
                        setOnClickListener {
                            val popup = PopupMenu(context, switchRegionButton)
                            popup.menuInflater.inflate(R.menu.region_switcher, popup.menu)
                            popup.show()
                            popup.setOnMenuItemClickListener {
                                when (it.itemId) {
                                    R.id.region_americas -> setParentRegion(1)
                                    R.id.region_europe -> setParentRegion(2)
                                    R.id.region_asia -> setParentRegion(3)
                                    R.id.region_oceania -> setParentRegion(4)
                                    else -> return@setOnMenuItemClickListener false
                                }

                                return@setOnMenuItemClickListener true
                            }
                        }
                    }
                }
            }

        }.getItemViewType { pos ->
            return@getItemViewType when {
                pos == 0 -> EXPLORE_HEADER_ITEM_TYPE
                childRegions.isEmpty() -> EXPLORE_AREA_ITEM_TYPE
                else -> EXPLORE_REGION_ITEM_TYPE
            }
        }
        .build()


    private fun nextRegion(region: SkiRegion, addToStack: Boolean = true) {
        setOnline()
        exploreLoadingIndicator.visibility = View.VISIBLE
        newIoThread {
            if (addToStack) {
                regionStack.add(region)
            }
            refreshRegions(region)

            if (!isOnline()) {
                Log.d("offlineCheck", "childIds: ${region.childIds}, childRegions: ${childRegions.map { it.id }}")
                Log.d("offlineCheck", "areaIds: ${region.areaIds}, childAreas: ${childAreas.map { it.id }}")
                mainThread {
                    setOffline {
                        nextRegion(region, false)
                    }
                }
            }
            mainThread {
                exploreLoadingIndicator.visibility = View.GONE
                exploreAdapter.notifyDataSetChanged()
                exploreRegionRecycler.scrollToPosition(0)
                exploreRegionRecycler.scheduleLayoutAnimation()
            }
        }
    }

    fun backRegion() {
        exploreLoadingIndicator.visibility = View.VISIBLE
        newIoThread {
            val region = regionStack.last()
            refreshRegions(region)
            if (!isOnline()) {
                Log.d("offlineCheck", "childIds: ${region.childIds}, childRegions: ${childRegions.map { it.id }}")
                Log.d("offlineCheck", "areaIds: ${region.areaIds}, childAreas: ${childAreas.map { it.id }}")
                mainThread {
                    setOffline {
                        backRegion()
                    }
                }

            }
            mainThread {
                exploreLoadingIndicator.visibility = View.GONE
                exploreAdapter.notifyDataSetChanged()
                exploreRegionRecycler.scrollToPosition(0)
                exploreRegionRecycler.scheduleLayoutAnimation()
            }
        }
    }

    private fun refreshRegions(region: SkiRegion) {
        childRegions = RegionsRepo.getRegions(region)
        childAreas = AreasRepo.getAreasByParent(region)
    }

    private fun Chip.setupChild(chipItem: ChipItem?) {

        if (chipItem == null) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
            text = chipItem.region?.name ?: chipItem.area?.name ?: ""
            setOnClickListener {
                if (chipItem.region != null) {
                    nextRegion(chipItem.region)
                } else if (chipItem.area != null) {
                    openAreaView(chipItem.area.id, context)
                }
            }
        }
    }

    private fun Chip.setIcon() {
        isChipIconVisible = true
        chipEndPadding = dpToPx(4).toFloat()
        textEndPadding = 0f
        textStartPadding = 0f
    }

    private fun Chip.setText() {
        isChipIconVisible = false
        chipEndPadding = dpToPx(6).toFloat()
        textEndPadding = dpToPx(6).toFloat()
        textStartPadding = dpToPx(8).toFloat()
    }

}

