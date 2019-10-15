package com.andb.apps.trails.pages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.andb.apps.trails.R
import com.andb.apps.trails.lists.RegionList
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.objects.SkiRegionTree
import com.andb.apps.trails.openAreaView
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.utils.mainThread
import com.andb.apps.trails.utils.newIoThread
import com.andb.apps.trails.utils.showIfAvailable
import com.andb.apps.trails.views.AreaItem
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.github.rongi.klaster.Klaster
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.explore_header.*
import kotlinx.android.synthetic.main.explore_layout.*
import kotlinx.android.synthetic.main.region_item.*
import kotlinx.coroutines.*

const val EXPLORE_HEADER_ITEM_TYPE = 98342
const val EXPLORE_AREA_ITEM_TYPE = 32940
const val EXPLORE_REGION_ITEM_TYPE = 34987

class ExploreFragment : Fragment() {

    val exploreAdapter by lazy { exploreAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.explore_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newIoThread {
            Log.d("onViewCreated", "setup RegionList")
            RegionList.setup()
            mainThread {
                Log.d("onViewCreated", "setParentRegion")
                setParentRegion(1, true)
            }
        }
    }

    private fun setParentRegion(id: Int, start: Boolean = false) {
        Log.d("setParentRegion", "id: $id")
        newIoThread {
            RegionList.backStack.apply {
                clear()
                add(RegionList.parentRegions[id - 1])
            }
            Log.d("setParentRegion", "parentRegions: ${RegionList.parentRegions}")
            mainThread {
                if (start) {
                    exploreRegionRecycler.layoutManager = LinearLayoutManager(context)
                    exploreRegionRecycler.adapter = exploreAdapter
                } else {
                    exploreAdapter.notifyDataSetChanged()
                    exploreRegionRecycler.scrollToPosition(0)
                    exploreRegionRecycler.scheduleLayoutAnimation()
                }
            }
        }
    }


    private fun exploreAdapter() = Klaster.get()
        .itemCount {
            RegionList.currentRegion().let {
                if (it.childRegions.isEmpty()) it.childAreas.size else it.childRegions.size
            } + 1
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
            RegionList.currentRegion().apply {
                when (itemViewType) {
                    EXPLORE_REGION_ITEM_TYPE -> {
                        val region = childRegions[position - 1]
                        regionName.text = region.name
                        regionMaps.showIfAvailable(region.mapCount, R.string.map_count)
                        chipChild(0, region, regionChildrenChip1)
                        chipChild(1, region, regionChildrenChip2)
                        itemView.setOnClickListener {
                            nextRegion(region)
                        }
                    }
                    EXPLORE_AREA_ITEM_TYPE -> {
                        val area = childAreas[position - 1]
                        (itemView as AreaItem).setup(area){favorite ->
                            area.favorite = favorite
                            newIoThread {
                                AreasRepo.areasDao.updateArea(area)
                            }
                        }
                    }
                    EXPLORE_HEADER_ITEM_TYPE -> {
                        exploreHeaderRegionName.text = RegionList.currentRegion().name
                        switchRegionButton.apply {
                            visibility = if (RegionList.backStack.size == 1) View.VISIBLE else View.GONE
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

            }

        }.getItemViewType { pos ->
            if (pos == 0) {
                return@getItemViewType EXPLORE_HEADER_ITEM_TYPE
            }
            if (RegionList.currentRegion().childRegions.isEmpty()) EXPLORE_AREA_ITEM_TYPE else EXPLORE_REGION_ITEM_TYPE
        }

        .build()


    private fun nextRegion(region: SkiRegionTree) {
        RegionList.backStack.add(region)
        exploreAdapter.notifyDataSetChanged()
        exploreRegionRecycler.scrollToPosition(0)
        exploreRegionRecycler.scheduleLayoutAnimation()
    }

    private fun chipChild(position: Int, region: SkiRegionTree, chip: Chip) {
        chip.apply {
            val chipText = chipText(position, region)
            if (chipText == "") {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = chipText
                setOnClickListener {
                    if (region.childRegions.isEmpty()) {
                        openAreaView(region.childAreas[position].id, context)
                    } else {
                        nextRegion(region.childRegions.sortedWith(Comparator { o1, o2 ->
                            o2.mapCount.compareTo(o1.mapCount)
                        })[position])
                    }
                }
            }
        }
    }

    private fun chipText(position: Int, region: SkiRegionTree): String {
        region.apply {
            return if (childRegions.isEmpty()) {
                if (childAreas.size > position) childAreas[position].name else ""
            } else {
                if (childRegions.size > position)
                    childRegions.sortedWith(Comparator { o1, o2 ->
                        o2.mapCount.compareTo(o1.mapCount)
                    })[position].name
                else
                    ""
            }
        }
    }

    fun isBackPossible(): Boolean = RegionList.backStack.size>1
    fun backRegion(){
        RegionList.drop()
        exploreAdapter.notifyDataSetChanged()
        exploreRegionRecycler.scrollToPosition(0)
        exploreRegionRecycler.scheduleLayoutAnimation()
    }
}


/*
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.andb.apps.trails.Prefs
import com.andb.apps.trails.R
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegionTree
import com.andb.apps.trails.openAreaView
import com.andb.apps.trails.utils.dpToPx
import com.andb.apps.trails.utils.newIoThread
import com.andb.apps.trails.utils.showIfAvailable
import com.andb.apps.trails.views.AreaItem
import com.github.rongi.klaster.Klaster
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.explore_header.*
import kotlinx.android.synthetic.main.explore_layout.*
import kotlinx.android.synthetic.main.offline_item.view.*
import kotlinx.android.synthetic.main.region_item.*
import org.koin.android.viewmodel.ext.android.viewModel


const val EXPLORE_HEADER_ITEM_TYPE = 98342
const val EXPLORE_AREA_ITEM_TYPE = 32940
const val EXPLORE_REGION_ITEM_TYPE = 34987

class ExploreFragment : Fragment() {

    private val exploreAdapter by lazy { exploreAdapter() }
    private var parentRegionTree: SkiRegionTree? = null

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
        switchRegionButton.setOnClickListener {
            val popup = PopupMenu(context, switchRegionButton)
            popup.menuInflater.inflate(R.menu.region_switcher, popup.menu)
            popup.show()
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.region_americas -> viewModel.setBaseRegion(1)
                    R.id.region_europe -> */
/*viewModel.*//*
setBaseRegion(2)
                    R.id.region_asia -> */
/*viewModel.*//*
setBaseRegion(3)
                    R.id.region_oceania -> */
/*viewModel.*//*
setBaseRegion(4)
                    else -> return@setOnMenuItemClickListener false
                }

                return@setOnMenuItemClickListener true
            }
        }

        exploreOfflineItem.offlineRefreshButton.setOnClickListener {
            refresh(viewModel.regionStack.lastOrNull())
        }

        if (viewModel.isFirstLoad()) {
            viewModel.setup(Prefs.startingRegion){
                setBaseRegion(Prefs.startingRegion)
            }
            //setBaseRegion(Prefs.startingRegion)
        }

        viewModel.parent.observe(viewLifecycleOwner, onParentChangeListener)
*/
/*        viewModel.childRegions.observe(viewLifecycleOwner, onChildRegionChangeListener)
        viewModel.childAreas.observe(viewLifecycleOwner, onChildAreaChangeListener)*//*

        //viewModel.offline.observe(viewLifecycleOwner, onOfflineChangeListener)
        viewModel.loading.observe(viewLifecycleOwner, onLoadingChangeListener)
    }

    private val onParentChangeListener = Observer<SkiRegionTree?> { tree ->
        val name = tree?.name ?: ""
        Log.d("nameChangeListener", "parent changed to $name")
        exploreHeaderRegionName.text = if (name.isNotEmpty()) name else getNameIfBase(viewModel.baseRegionOffline)
        switchRegionButton.visibility = if (viewModel.isBaseRegion()) View.VISIBLE else View.GONE
        parentRegionTree = tree
*/
/*        childRegions = tree?.childRegions?.filter { it.mapCount > 0 } ?: listOf()
        childAreas = tree?.childAreas ?: listOf()*//*

        exploreAdapter.notifyDataSetChanged()
        exploreRegionRecycler.scheduleLayoutAnimation()
    }


    fun setBaseRegion(id: Int){
        viewModel.setBaseRegion(id)
        val tree = viewModel.regionStack.last()
        refresh(tree)
    }

    private fun nextRegion(regionTree: SkiRegionTree){
        viewModel.regionStack.add(regionTree)
        refresh(regionTree)
    }

    fun back(){
        viewModel.regionStack = viewModel.regionStack.dropLast(1).toMutableList()
        refresh(viewModel.regionStack.last())
    }


    private fun refresh(regionTree: SkiRegionTree?) {
        val name = regionTree?.name ?: ""
        exploreHeaderRegionName.text = if (name.isNotEmpty()) name else getNameIfBase(viewModel.baseRegionOffline)
        if(regionTree!=null){
            exploreOfflineItem.visibility = View.GONE
        }else{
            exploreOfflineItem.visibility = View.VISIBLE
        }
        parentRegionTree = regionTree

        exploreAdapter.notifyDataSetChanged()
        exploreRegionRecycler.scheduleLayoutAnimation()
    }

    private fun getNameIfBase(id: Int): String {
        return when (id) {
            1 -> resources.getString(R.string.menu_region_americas)
            2 -> resources.getString(R.string.menu_region_europe)
            3 -> resources.getString(R.string.menu_region_asia)
            4 -> resources.getString(R.string.menu_region_oceania)
            else -> ""
        }
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
            parentRegionTree?.let {
                it.childAreas.size + it.childRegions.size //one of which will be zero
            } ?: 0
        }

        .view { viewType, parent ->
            when (viewType) {
                EXPLORE_REGION_ITEM_TYPE -> layoutInflater.inflate(com.andb.apps.trails.R.layout.region_item, parent, false)
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
            parentRegionTree?.apply {
                when (itemViewType) {
                    EXPLORE_REGION_ITEM_TYPE -> {
                        val region = childRegions[adapterPosition]

                        regionName.text = region.name
                        regionMaps.showIfAvailable(region.mapCount, com.andb.apps.trails.R.string.map_count)
                        itemView.setOnClickListener {
                            //viewModel.nextRegion(region)
                            nextRegion(region)
                        }

                        regionChildrenChip1.visibility = if ((region.childRegions.size + region.childAreas.size) > 0) View.VISIBLE else View.GONE
                        regionChildrenChip2.visibility = if ((region.childRegions.size + region.childAreas.size) > 1) View.VISIBLE else View.GONE


*/
/*                        (regionChildrenChip1 and regionChildrenChip2).applyEach {
                            text = ""
                            val progressDrawable = CircularProgressDrawable(requireContext()).apply {
                                setColorSchemeColors(resources.getColor(R.color.colorAccent))
                                strokeWidth = 4f
                                start()
                            }
                            setIconOnly(progressDrawable)
                        }*//*


                        val items = region.childRegions.sortedByDescending { it.mapCount }.map { ChipItem(region.id, region = it) } + region.childAreas.sortedByDescending { it.maps.size }.map { ChipItem(region.id, area = it) }
                        regionChildrenChip1.setupChild(items.getOrNull(0))
                        regionChildrenChip2.setupChild(items.getOrNull(1))
                    }
                    EXPLORE_AREA_ITEM_TYPE -> {
                        val area = childAreas[position]
                        (itemView as AreaItem).setup(area) {
                            area.favorite = it
                            newIoThread {
                                areasDao().updateArea(area)
                            }
                        }
                    }
                }
            }


        }.getItemViewType { _ ->
            return@getItemViewType when {
                parentRegionTree?.childRegions?.isEmpty() ?: false -> EXPLORE_AREA_ITEM_TYPE
                else -> EXPLORE_REGION_ITEM_TYPE
            }
        }
        .getItemId { pos ->
            return@getItemId parentRegionTree?.let {
                when {
                    it.childRegions.isEmpty() -> it.childAreas[pos].id
                    else -> it.childRegions[pos].id
                }.toLong()
            } ?: 0

        }
        .build()

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
                    //viewModel.nextRegion(chipItem.region)
                    nextRegion(chipItem.region)
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

*/
