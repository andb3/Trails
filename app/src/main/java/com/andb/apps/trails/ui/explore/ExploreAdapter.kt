package com.andb.apps.trails.ui.explore

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.andb.apps.trails.R
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiRegionTree
import com.andb.apps.trails.ui.common.AreaItem
import com.andb.apps.trails.util.*
import com.google.android.material.chip.Chip
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller
import kotlinx.android.synthetic.main.explore_header.view.*
import kotlinx.android.synthetic.main.offline_item.view.*
import kotlinx.android.synthetic.main.region_item.view.*

const val EXPLORE_HEADER_ITEM_TYPE = 98342
const val EXPLORE_AREA_ITEM_TYPE = 32940
const val EXPLORE_REGION_ITEM_TYPE = 34987
const val EXPLORE_OFFLINE_ITEM_TYPE = 84123

class ExploreAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    RecyclerViewFastScroller.OnPopupTextUpdate {

    private var parentTree: SkiRegionTree? = null
    private var offline = true

    private var onRegionClickListener: ((SkiRegionTree) -> Unit)? = null
    private var onAreaClickListener: ((SkiArea) -> Unit)? = null
    private var onAreaFavoriteListener: ((SkiArea, Boolean) -> Unit)? = null
    private var onOfflineRefreshListener: (() -> Unit)? = null
    private var onHeaderSwitchListener: ((Int) -> Unit)? = null

    private var attachedRecyclerView: RecyclerView? = null

    class VH(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = when (viewType) {
            EXPLORE_HEADER_ITEM_TYPE -> LayoutInflater.from(parent.context).inflate(
                R.layout.explore_header,
                parent,
                false
            )
            EXPLORE_REGION_ITEM_TYPE -> LayoutInflater.from(parent.context).inflate(
                R.layout.region_item,
                parent,
                false
            )
            EXPLORE_AREA_ITEM_TYPE -> AreaItem(parent.context).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            else -> LayoutInflater.from(parent.context).inflate(
                R.layout.offline_item,
                parent,
                false
            )
        }

        return VH(view)
    }

    override fun getItemCount(): Int {
        val header = 1
        val children = parentTree?.run { childRegions.size + childAreas.size } ?: 0
        val offline = if (offline) 1 else 0
        return header + children + offline
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            EXPLORE_REGION_ITEM_TYPE -> {
                val regionTree = parentTree!!.childRegions[position - 1]
                holder.itemView.apply {
                    regionName.text = regionTree.name
                    regionMaps.showIfAvailable(regionTree.mapCount, R.string.map_count)
                    setOnClickListener {
                        onRegionClickListener?.invoke(regionTree)
                    }

                    regionChildrenChip1.visibility =
                        if ((regionTree.childRegions.size + regionTree.childAreas.size) > 0) View.VISIBLE else View.GONE
                    regionChildrenChip2.visibility =
                        if ((regionTree.childRegions.size + regionTree.childAreas.size) > 1) View.VISIBLE else View.GONE



                    (regionChildrenChip1 and regionChildrenChip2).applyEach {
                        text = ""
                        val progressDrawable = CircularProgressDrawable(context).apply {
                            setColorSchemeColors(resources.getColor(R.color.colorAccent))
                            strokeWidth = 4f
                            start()
                        }
                        setIconOnly(progressDrawable)
                    }

                    val regionItems = regionTree.childRegions
                        .sortedByDescending { it.mapCount }
                        .map { ChipItem(regionTree.id, region = it) }
                    val areaItems = regionTree.childAreas
                        .sortedByDescending { it.maps.size }
                        .map { ChipItem(regionTree.id, area = it) }
                    val items = regionItems + areaItems

                    regionChildrenChip1.setupChild(items.getOrNull(0))
                    regionChildrenChip2.setupChild(items.getOrNull(1))
                }
            }
            EXPLORE_AREA_ITEM_TYPE -> {
                //val item = backingList[position-1].area!!
                val area = parentTree!!.childAreas[position - 1]
                (holder.itemView as AreaItem).apply {
                    setup(area)
                    setOnFavoriteListener { area, favorite ->
                        onAreaFavoriteListener?.invoke(area, favorite)
                    }
                }
            }
            EXPLORE_HEADER_ITEM_TYPE -> {
                holder.itemView.apply {
                    exploreHeaderRegionName.text =
                        if (!parentTree?.name.isNullOrEmpty()) parentTree!!.name else resources.getString(
                            R.string.offline_error_title
                        )
                    switchRegionButton.apply {
                        visibility = if (parentTree?.isBase() != false) View.VISIBLE else View.GONE
                        setOnClickListener {
                            val popup = PopupMenu(context, switchRegionButton)
                            popup.menuInflater.inflate(R.menu.region_switcher, popup.menu)
                            popup.show()
                            popup.setOnMenuItemClickListener {
                                when (it.itemId) {
                                    R.id.region_americas -> onHeaderSwitchListener?.invoke(1)
                                    R.id.region_europe -> onHeaderSwitchListener?.invoke(2)
                                    R.id.region_asia -> onHeaderSwitchListener?.invoke(3)
                                    R.id.region_oceania -> onHeaderSwitchListener?.invoke(4)
                                    else -> return@setOnMenuItemClickListener false
                                }

                                return@setOnMenuItemClickListener true
                            }
                        }
                    }
                }
            }
            EXPLORE_OFFLINE_ITEM_TYPE -> {
                holder.itemView.offlineRefreshButton.setOnClickListener {
                    onOfflineRefreshListener?.invoke()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val childItem = 1 <= position && position <= (parentTree?.childrenSize() ?: 0)
        return when {
            position == 0 -> EXPLORE_HEADER_ITEM_TYPE
            childItem -> if (parentTree!!.childRegions.isNotEmpty()) EXPLORE_REGION_ITEM_TYPE else EXPLORE_AREA_ITEM_TYPE
            else -> EXPLORE_OFFLINE_ITEM_TYPE
        }
    }


    fun updateTree(tree: SkiRegionTree) {
        val diff = tree.id == parentTree?.id
        Log.d("updateTree", "diff ($diff) - newID: ${tree.id}, oldID: ${parentTree?.id}")
        if (!diff) {
            parentTree = tree
            offline = tree.offline
            notifyDataSetChanged()
            attachedRecyclerView?.scheduleLayoutAnimation()
            attachedRecyclerView?.scrollToPosition(0)
        } else {
            newIoThread {
                val regionDiff = DiffUtil.calculateDiff(
                    RegionDiffCallback(
                        parentTree!!.childRegions,
                        tree.childRegions
                    )
                )
                val areaDiff = DiffUtil.calculateDiff(
                    AreaDiffCallback(
                        parentTree!!.childAreas,
                        tree.childAreas
                    )
                )
                mainThread {
                    parentTree = tree
                    offline = tree.offline
                    regionDiff.dispatchUpdatesTo(this@ExploreAdapter)
                    areaDiff.dispatchUpdatesTo(this@ExploreAdapter)
                }
            }
        }
    }

    fun setOnRegionClickListener(callback: (SkiRegionTree) -> Unit) {
        onRegionClickListener = callback
    }

    fun setOnAreaClickListener(callback: (SkiArea) -> Unit) {
        onAreaClickListener = callback
    }

    fun setOnAreaFavoriteListener(callback: (SkiArea, Boolean) -> Unit) {
        onAreaFavoriteListener = callback
    }

    fun setOnHeaderSwitchListener(callback: (Int) -> Unit) {
        onHeaderSwitchListener = callback
    }

    fun setOnOfflineRefreshListener(callback: () -> Unit) {
        onOfflineRefreshListener = callback
    }

    private fun Chip.setupChild(chipItem: ChipItem?) {

        when {
            chipItem == null -> visibility = View.GONE
            chipItem.area != null -> {
                visibility = View.VISIBLE
                text = chipItem.area.name
                setTextOnly()
                setOnClickListener {
                    onAreaClickListener?.invoke(chipItem.area)
                }
            }
            chipItem.region != null -> {
                visibility = View.VISIBLE
                text = chipItem.region.name
                setTextOnly()
                setOnClickListener {
                    onRegionClickListener?.invoke(chipItem.region)
                }
            }
            else -> {
                visibility = View.VISIBLE
                text = ""
                setIconOnly(resources.getDrawable(R.drawable.ic_cloud_off_black_24dp))
            }
        }

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        attachedRecyclerView = recyclerView
    }

    override fun onChange(position: Int): CharSequence {
        val treeFinal = parentTree
        return when {
            treeFinal == null || position <= 0 -> ""
            treeFinal.childAreas.isNotEmpty() && position - 1 < treeFinal.childAreas.size -> treeFinal.childAreas[position - 1].name.subSequence(
                0,
                1
            )
            treeFinal.childRegions.isNotEmpty() && position - 1 < treeFinal.childRegions.size -> treeFinal.childRegions[position - 1].name.subSequence(
                0,
                1
            )
            else -> ""

        }
    }

}

private class RegionDiffCallback(oldList: List<SkiRegionTree>, newList: List<SkiRegionTree>) :
    DiffCallback<SkiRegionTree>(oldList, newList) {
    override fun areItemsTheSame(oldItem: SkiRegionTree, newItem: SkiRegionTree): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SkiRegionTree, newItem: SkiRegionTree): Boolean {
        return oldItem == newItem
    }
}

private class AreaDiffCallback(oldList: List<SkiArea>, newAreas: List<SkiArea>) :
    DiffCallback<SkiArea>(oldList, newAreas) {
    override fun areItemsTheSame(oldItem: SkiArea, newItem: SkiArea): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SkiArea, newItem: SkiArea): Boolean {
        return oldItem == newItem
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

class ChipItem(val parentID: Int, val region: SkiRegionTree? = null, val area: SkiArea? = null)