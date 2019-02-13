package com.andb.apps.trails.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.trails.AreaViewFragment
import com.andb.apps.trails.R
import com.andb.apps.trails.lists.FavoritesList
import com.andb.apps.trails.lists.RegionList
import com.andb.apps.trails.objects.BaseSkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.openAreaView
import com.andb.apps.trails.utils.Utils
import com.andb.apps.trails.utils.dpToPx
import com.andb.apps.trails.views.GlideApp
import com.andb.apps.trails.views.items.AreaItem
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.github.rongi.klaster.Klaster
import com.google.android.material.chip.Chip
import com.like.LikeButton
import com.like.OnLikeListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.explore_header.*
import kotlinx.android.synthetic.main.area_item.*
import kotlinx.android.synthetic.main.region_item.*
import kotlinx.android.synthetic.main.explore_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main

const val EXPLORE_HEADER_ITEM_TYPE = 98342
const val EXPLORE_AREA_ITEM_TYPE = 32940
const val EXPLORE_REGION_ITEM_TYPE = 34987

class ExploreFragment : Fragment() {

    lateinit var exploreAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.explore_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
            RegionList.setup()
            withContext(Dispatchers.Main) {
                setParentRegion(1, true)
            }
        }
    }

    fun glideRV(){
        val preloader = ViewPreloadSizeProvider<ImageView>()
    }

    private fun setParentRegion(id: Int, start: Boolean = false) {
        CoroutineScope(Dispatchers.IO).launch {
            RegionList.backStack.apply {
                clear()
                add(RegionList.parentRegions[id - 1])
            }
            withContext(Dispatchers.Main) {
                if (start) {
                    exploreAdapter = exploreAdapter()
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
                if (it.children.isEmpty()) it.areas.size else it.children.size
            }+1
        }
        .view { viewType, parent ->

            when (viewType) {
                EXPLORE_REGION_ITEM_TYPE -> layoutInflater.inflate(R.layout.region_item, parent, false)
                EXPLORE_AREA_ITEM_TYPE -> AreaItem(context?: this.requireContext()).also {
                    it.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                else->layoutInflater.inflate(R.layout.explore_header, parent, false)
            }
        }
        .bind { position ->
            RegionList.currentRegion().apply {
                when(itemViewType){
                    EXPLORE_REGION_ITEM_TYPE->{
                        val region = children[position-1]
                        regionName.text = region.name
                        Utils.showIfAvailible(region.mapCount, regionMaps, R.string.map_count)
                        chipChild(0, region, regionChildrenChip1)
                        chipChild(1, region, regionChildrenChip2)
                        itemView.setOnClickListener {
                            nextRegion(region)
                        }
                    }
                    EXPLORE_AREA_ITEM_TYPE->{
                        val area = areas[position-1]
                        (itemView as AreaItem).setup(area)
                    }
                    EXPLORE_HEADER_ITEM_TYPE->{
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

        }.getItemViewType {pos->
            if(pos==0){
                return@getItemViewType EXPLORE_HEADER_ITEM_TYPE
            }
            if (RegionList.currentRegion().children.isEmpty()) EXPLORE_AREA_ITEM_TYPE else EXPLORE_REGION_ITEM_TYPE
        }

        .build()



    fun nextRegion(region: SkiRegion) {
        RegionList.backStack.add(region)
        exploreAdapter.notifyDataSetChanged()
        exploreRegionRecycler.scrollToPosition(0)
        exploreRegionRecycler.scheduleLayoutAnimation()
    }

    private fun chipChild(position: Int, region: SkiRegion, chip: Chip) {
        chip.apply {
            val chipText = chipText(position, region)
            if (chipText == "") {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = chipText
                setOnClickListener {
                    if (region.children.isEmpty()) {
                        openAreaView(region.areas[position], context, chip)
                    } else {
                        nextRegion(region.children.sortedWith(Comparator { o1, o2 ->
                            o2.mapCount.compareTo(o1.mapCount)
                        })[position])
                    }
                }
            }
        }
    }

    private fun chipText(position: Int, region: SkiRegion): String {
        region.apply {
            return if (children.isEmpty()) {
                if (areas.size > position) areas[position].name else ""
            } else {
                if (children.size > position)
                    children.sortedWith(Comparator { o1, o2 ->
                        o2.mapCount.compareTo(o1.mapCount)
                    })[position].name
                else
                    ""
            }
        }
    }
}

