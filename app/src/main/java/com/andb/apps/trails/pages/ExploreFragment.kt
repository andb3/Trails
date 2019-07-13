package com.andb.apps.trails.pages

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.andb.apps.trails.ChipItem
import com.andb.apps.trails.ExploreViewModel
import com.andb.apps.trails.R
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.openAreaView
import com.andb.apps.trails.utils.*
import com.andb.apps.trails.views.AreaItem
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

    private val exploreAdapter by lazy { exploreAdapter() }
    private var childRegions: List<SkiRegion> = listOf()
    private var childAreas: List<SkiArea> = listOf()
    private var chips: List<ChipItem> = listOf()

    val viewModel: ExploreViewModel by lazy {
        ViewModelProviders.of(this).get(ExploreViewModel::class.java)
    }

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
        switchRegionButton.setOnClickListener {
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

        exploreOfflineItem.offlineRefreshButton.setOnClickListener {
            viewModel.refresh()
        }

        Log.d("firstLoad", "first load: ${viewModel.isFirstLoad()}")
        if (viewModel.isFirstLoad()) {
            viewModel.setBaseRegion(1)
        }

        viewModel.getParentRegionName().observe(viewLifecycleOwner, onParentNameChangeListener)
        viewModel.getChildRegions().observe(viewLifecycleOwner, onChildRegionChangeListener)
        viewModel.getChildAreas().observe(viewLifecycleOwner, onChildAreaChangeListener)
        viewModel.getOffline().observe(viewLifecycleOwner, onOfflineChangeListener)
        viewModel.getLoadingState().observe(viewLifecycleOwner, onLoadingChangeListener)
        viewModel.getChips().observe(viewLifecycleOwner, onChipChangeListener)
    }

    private val onParentNameChangeListener = Observer<String> { name ->
        Log.d("nameChangeListener", "name changed to $name")
        exploreHeaderRegionName.text = name
        switchRegionButton.visibility = if (viewModel.isBaseRegion()) View.VISIBLE else View.GONE
        childRegions = listOf()
        childAreas = listOf()
        exploreAdapter.notifyDataSetChanged()
        exploreRegionRecycler.scrollToPosition(0)
        exploreRegionRecycler.scheduleLayoutAnimation()
    }

    private val onChildRegionChangeListener = Observer<List<SkiRegion?>> { regions ->
        val newRegions = regions.filterNotNull().filter { it.mapCount != 0 }
            .sortedWith(Comparator { o1, o2 -> if (viewModel.isBaseRegion()) o2.mapCount.compareTo(o1.mapCount) else o1.name.compareTo(o2.name) })

        if (regions.isNotEmpty()) {
            childAreas = listOf() //since updates to one list sometimes aren't fired on the other's change
        }
        //reload with animation
        Log.d("regionDiff", "reload")
        childRegions = newRegions
        exploreAdapter.notifyDataSetChanged()
        exploreNestedScrollView.scrollTo(0, 0)
        exploreRegionRecycler.scheduleLayoutAnimation()

        Log.d("regionsChanged", "new regions: ${newRegions.map { it.name }}")


    }

    private val onChildAreaChangeListener = Observer<List<SkiArea?>> { areas ->
        val newAreas = areas.toList().filterNotNull().sortedBy { it.name }

        if (areas.isNotEmpty()) {
            childRegions = listOf()
        }
        Log.d("areaDiff", "reload")
        childAreas = newAreas
        exploreAdapter.notifyDataSetChanged()
        exploreNestedScrollView.scrollTo(0, 0)
        exploreRegionRecycler.scheduleLayoutAnimation()

        Log.d("areasChanged", "new areas: ${newAreas.map { it.name }}")


    }

    private val onChipChangeListener = Observer<List<ChipItem>> { chips ->
        this.chips = chips
        exploreAdapter.notifyDataSetChanged()
    }

    private val onOfflineChangeListener = Observer<Boolean> { offline ->
        if (offline) {
            exploreOfflineItem.visibility = View.VISIBLE
        } else {
            exploreOfflineItem.visibility = View.GONE
        }
    }

    private val onLoadingChangeListener = Observer<Boolean> { loading ->
        if (loading) {
            exploreLoadingIndicator.visibility = View.VISIBLE
        } else {
            exploreLoadingIndicator.visibility = View.GONE
        }
    }


    private fun exploreAdapter() = Klaster.get()
        .itemCount {
            when {
                childRegions.isEmpty() -> childAreas.size
                else -> childRegions.size
            }
        }

        .view { viewType, parent ->
            when (viewType) {
                EXPLORE_REGION_ITEM_TYPE -> layoutInflater.inflate(R.layout.region_item, parent, false)
                else -> AreaItem(requireContext()).also {
                    //EXPLORE_AREA_ITEM_TYPE
                    it.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            }
        }
        .bind { position ->
            when (itemViewType) {
                EXPLORE_REGION_ITEM_TYPE -> {
/*                    val payload: Any? = payloads.firstOrNull()
                    if(payload.isListOf<ChipItem>()) {*/
                    val region = childRegions[adapterPosition]

                    regionName.text = region.name
                    regionMaps.showIfAvailable(region.mapCount, R.string.map_count)
                    itemView.setOnClickListener {
                        viewModel.nextRegion(region)
                    }

                    regionChildrenChip1.visibility = if ((region.childIds.size + region.areaIds.size) >= 1) View.VISIBLE else View.GONE
                    regionChildrenChip2.visibility = if ((region.childIds.size + region.areaIds.size) >= 2) View.VISIBLE else View.GONE

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

                        (regionChildrenChip1 and regionChildrenChip2).applyEach {
                            visibility = View.VISIBLE
                            text = ""
                            setIconOnly(createLoadingDrawable())
                        }
                    }
                    /*}else{
                        payload as List<ChipItem>
                        regionChildrenChip1.setupChild(payload.getOrNull(0))
                        regionChildrenChip2.setupChild(payload.getOrNull(1))
                    }*/


                }
                EXPLORE_AREA_ITEM_TYPE -> {
                    (itemView as AreaItem).setup(childAreas[position])
                }
            }

        }.getItemViewType { pos ->
            return@getItemViewType when {
                childRegions.isEmpty() -> EXPLORE_AREA_ITEM_TYPE
                else -> EXPLORE_REGION_ITEM_TYPE
            }
        }
        .getItemId { pos ->
            return@getItemId when {
                childRegions.isEmpty() -> childAreas[pos].id
                else -> childRegions[pos].id
            }.toLong()
        }
        .build()

    private class RegionDiffCallback(oldList: List<SkiRegion>, newList: List<SkiRegion>) :
        DiffCallback<SkiRegion>(oldList, newList) {
        override fun areItemsTheSame(oldItem: SkiRegion, newItem: SkiRegion): Boolean {
            return oldItem.id == newItem.id
        }
    }

    private class AreaDiffCallback(oldList: List<SkiArea>, newTasks: List<SkiArea>) :
        DiffCallback<SkiArea>(oldList, newTasks) {
        override fun areItemsTheSame(oldItem: SkiArea, newItem: SkiArea): Boolean {
            return oldItem.id == newItem.id
        }
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
                    viewModel.nextRegion(chipItem.region)
                }
            }
            else -> {
                visibility = View.VISIBLE
                text = ""
                setIconOnly(resources.getDrawable(R.drawable.ic_cloud_off_black_24dp))
            }
        }

    }

}

fun Chip.setIconOnly(icon: Drawable? = null) {
    if (icon != null) {
        chipIcon = icon
    }
    isChipIconVisible = true
    chipEndPadding = dpToPx(4).toFloat()
    textEndPadding = 0f
    textStartPadding = 0f
}

fun Chip.setTextOnly() {
    isChipIconVisible = false
    chipEndPadding = dpToPx(6).toFloat()
    textEndPadding = dpToPx(6).toFloat()
    textStartPadding = dpToPx(8).toFloat()
}

