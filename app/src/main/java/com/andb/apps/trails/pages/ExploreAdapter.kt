package com.andb.apps.trails.pages

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.andb.apps.trails.R
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.openAreaView
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.utils.*
import com.andb.apps.trails.views.AreaItem
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.region_item.*
import kotlinx.android.synthetic.main.region_item.view.*

const val EXPLORE_HEADER_ITEM_TYPE = 98342
const val EXPLORE_AREA_ITEM_TYPE = 32940
const val EXPLORE_REGION_ITEM_TYPE = 34987
const val EXPLORE_OFFLINE_ITEM_TYPE = 84123

class ExploreAdapter(val parentFragment: ExploreFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val backingList = mutableListOf<ExploreItem>()

    class VH(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = when (viewType) {

            EXPLORE_REGION_ITEM_TYPE -> LayoutInflater.from(parent.context).inflate(R.layout.region_item, parent, false)
            else -> AreaItem(parent.context).also {
                //EXPLORE_AREA_ITEM_TYPE
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        return VH(view)
    }

    override fun getItemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            EXPLORE_REGION_ITEM_TYPE -> {
                /*val region = backingList[position - 1].region!!
                holder.itemView.apply {
                    regionName.text = region.name
                    regionMaps.showIfAvailable(region.mapCount, R.string.map_count)
                    setOnClickListener {
                        //viewModel.nextRegion(region)
                        parentFragment.nextRegion(region)
                    }

                    regionChildrenChip1.visibility = if ((region.childRegionIDs.size + region.childAreaIDs.size) > 0) View.VISIBLE else View.GONE
                    regionChildrenChip2.visibility = if ((region.childRegionIDs.size + region.childAreaIDs.size) > 1) View.VISIBLE else View.GONE



                    (regionChildrenChip1 and regionChildrenChip2).applyEach {
                        text = ""
                        val progressDrawable = CircularProgressDrawable(context).apply {
                            setColorSchemeColors(resources.getColor(R.color.colorAccent))
                            strokeWidth = 4f
                            start()
                        }
                        setIconOnly(progressDrawable)
                    }


                    val items = region.childRegions.sortedByDescending { it.mapCount }.map { ChipItem(region.id, region = it) } + region.childAreas.sortedByDescending { it.maps.size }.map { ChipItem(region.id, area = it) }
                    regionChildrenChip1.setupChild(items.getOrNull(0))
                    regionChildrenChip2.setupChild(items.getOrNull(1))
                }*/
            }
            EXPLORE_AREA_ITEM_TYPE -> {
                val item = backingList[position-1].area!!
                (holder.itemView as AreaItem).setup(item) { favorite ->
                    newIoThread {
                        val area = AreasRepo.getAreaByID(item.id)
                        area?.also {
                            it.favorite = favorite
                            areasDao().updateArea(area)
                        }
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> EXPLORE_HEADER_ITEM_TYPE
            backingList[position - 1].isRegion() -> EXPLORE_REGION_ITEM_TYPE
            backingList[position - 1].isArea() -> EXPLORE_AREA_ITEM_TYPE
            else -> EXPLORE_OFFLINE_ITEM_TYPE
        }
    }

    fun updateAreas(list: List<SkiArea>){
        val newList = list.map { ExploreItem(null, it) }
        val diff = newList.intersects(backingList)
    }

    fun updateRegions(list: List<SkiRegion>) {
        val newList = list.map { ExploreItem(it, null) }
        val diff = newList.intersects(backingList)
    }

    class ExploreItem(val region: SkiRegion?, val area: SkiArea?) {
        fun isRegion() = region != null
        fun isArea() = area != null
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
                    parentFragment.nextRegion(chipItem.region)
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

