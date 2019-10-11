package com.andb.apps.trails.pages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.trails.R
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.utils.intersects
import com.andb.apps.trails.utils.ioThread
import com.andb.apps.trails.utils.newIoThread
import com.andb.apps.trails.views.AreaItem

const val EXPLORE_ITEM_REGION = 483323
const val EXPLORE_ITEM_AREA = 12783


class ExploreAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val backingList = mutableListOf<ExploreItem>()

    class VH(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = when (viewType) {
            EXPLORE_ITEM_REGION -> LayoutInflater.from(parent.context).inflate(R.layout.region_item, parent, false)
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
            EXPLORE_ITEM_REGION -> {

            }
            EXPLORE_ITEM_AREA -> {
                val item = backingList[position] as ExploreItem.Area
                (holder.itemView as AreaItem).setup(item.id, item.name, item.maps, item.previewID, item.favorite) { favorite ->
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
        return when (backingList[position]) {
            is ExploreItem.Region -> EXPLORE_ITEM_REGION
            is ExploreItem.Area -> EXPLORE_ITEM_AREA
        }
    }

/*    fun updateAreas(list: List<SkiArea>){
        val newList = list.map { ExploreItem.Area(it.id, it.name, it.maps.size, it.mapPreviewID(), it.favorite) }
        val diff = newList.intersects(backingList)
    }

    fun updateRegions(list: List<SkiRegion>) {
        val newList = list.map { ExploreItem.Region(it.id, it.name, it.mapCount, Pair(it.)) }
        val diff = newList.intersects(backingList)
    }*/

    sealed class ExploreItem(val id: Int, val name: String, val maps: Int) {
        class Region(id: Int, name: String, maps: Int, val chips: Pair<String, String>) :
            ExploreItem(id, name, maps)

        class Area(id: Int, name: String, maps: Int, val previewID: Int?, val favorite: Boolean) :
            ExploreItem(id, name, maps)
    }
}

