package com.andb.apps.trails

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.TransitionInflater
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.utils.*
import com.andb.apps.trails.views.MapItem
import com.github.rongi.klaster.Klaster
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.area_layout.*
import kotlinx.android.synthetic.main.offline_item.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.map_item.view.*


class AreaViewFragment : Fragment() {

    private val mapAdapter by lazy { mapAdapter() }
    private var skiArea: SkiArea? = null
    private var maps = listOf<SkiMap>()
    private var areaKey = -1

    private val viewModel: AreaViewModel by viewModel()

    private var clickedPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        areaKey = arguments!!.getInt("areaKey")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.area_layout, container, false)
        //prepareTransitions()
        //postponeEnterTransition()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapListRecycler.layoutManager = GridLayoutManager(context, 2)
        areaNested.apply {
            setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
                Log.d("areaRecycler", "scrolled")
                if (scrollY > oldScrollY) {
                    areaViewFab?.hide()
                } else {
                    areaViewFab?.show()
                }
            }
        }
        setupFab()
        mapListRecycler.adapter = mapAdapter
        viewModel.skiArea.observe(viewLifecycleOwner, areaObserver)
        viewModel.maps.observe(viewLifecycleOwner, mapsObserver)
        viewModel.regions.observe(viewLifecycleOwner, regionsObserver)
        viewModel.offline.observe(viewLifecycleOwner, offlineObserver)
        viewModel.loadArea(areaKey)

    }

    private val areaObserver = Observer<SkiArea?> { newArea ->

        this.skiArea = newArea

        if (newArea != null) {
            areaViewName.text = newArea.name
            newArea.details?.apply {
                areaLiftCount.showIfAvailable(liftCount, R.string.area_lift_count_text)
                areaRunCount.showIfAvailable(runCount, R.string.area_run_count_text)
                areaOpeningYear.showIfAvailable(openingYear, R.string.area_opening_year_text)
                areaWebsite.showIfAvailable(website, R.string.area_website_text)
            }
        } else {
            setOffline {
                viewModel.loadArea(id)
            }
        }
    }

    private val mapsObserver = Observer<List<SkiMap>> {newMaps->
        this.maps = newMaps.sortedByDescending { it.year }
        if (maps.map { it.id }.toSet() != skiArea?.maps?.toSet()) {
            Log.d("areaFragmentOffline", "newMaps: ${maps.map {it.id}}, area maps: ${skiArea?.maps}")
            viewModel.offline.postValue(true)
        }
        areaLoadingIndicator.visibility = View.GONE
        mapAdapter.notifyDataSetChanged()
        mapListRecycler.scrollToPosition(0)
        mapListRecycler.scheduleLayoutAnimation()
    }

    private val regionsObserver = Observer<List<SkiRegion>> { regions ->
        val string = regions.joinToString { region -> region.name }
        areaViewRegions.text = String.format(getString(R.string.area_regions_text), string)
    }

    private val offlineObserver = Observer<Boolean> { offline->
        if (offline){
            setOffline{
                areaLoadingIndicator.visibility = View.VISIBLE
                viewModel.loadArea(id)
            }
        }else{
            setOnline()
        }
    }

    private fun setupFab() {
        areaViewFab.apply {
            imageTintList = ColorStateList.valueOf(Color.WHITE)
            setOnClickListener {
                toggleInfo(this)
            }
        }
        areaViewDim.setOnClickListener {
            if (isTranslated()) {
                toggleInfo(areaViewFab)
            }
        }

    }

    private fun isTranslated(): Boolean {
        return areaInfoCard.translationY != 0f
    }

    private fun toggleInfo(fab: FloatingActionButton) {
        fab.apply {
            val translated = isTranslated()
            val translateCard: Float
            val translateFab: Float
            val alpha: Float
            if (!translated) {
                translateCard = -areaInfoCard.height - dpToPx(8).toFloat()
                translateFab = -areaInfoCard.height + dpToPx(8).toFloat()
                alpha = .7f
                setImageDrawable(resources.getDrawable(R.drawable.ic_expand_more_black_24dp))
                areaViewDim.visibility = View.VISIBLE
                areaInfoCard.visibility = View.VISIBLE
            } else {
                translateCard = 0f
                translateFab = 0f
                alpha = 0f
                setImageDrawable(resources.getDrawable(R.drawable.ic_info_outline_black_24dp))
                areaViewDim.visibility = View.GONE
            }
            areaViewDim.animate().alpha(alpha)
            areaInfoCard.animate().translationY(translateCard).withEndAction {
                if (translated) {
                    areaInfoCard?.visibility = View.INVISIBLE
                }
            }
            this.animate().translationY(translateFab)
        }
    }


    fun setOffline(onRefresh: ((View) -> Unit)? = null) {
        areaLoadingIndicator.visibility = View.GONE
        areaOfflineItem.visibility = View.VISIBLE
        areaOfflineItem.offlineRefreshButton.setOnClickListener(onRefresh)
    }

    fun setOnline() {
        areaViewFab.visibility = View.VISIBLE
        areaOfflineItem.visibility = View.GONE
    }

    private fun mapAdapter() = Klaster.get()
        .itemCount { maps.size }
        .view { _, _ ->
            MapItem(requireContext()).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
        .bind { position ->
            (itemView as MapItem).setup(maps[position], skiArea?.name ?: ""){
                clickedPosition = position
            }
        }
        .build()

/*    private fun prepareTransitions(){
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(R.transition.image_shared_element_transition)
        setExitSharedElementCallback(object : SharedElementCallback(){
            override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
                // Locate the ViewHolder for the clicked position.
                val selectedViewHolder = mapListRecycler.findViewHolderForAdapterPosition(clickedPosition) ?: return

                // Map the first shared element name to the child ImageView.
                sharedElements[names[0]] = selectedViewHolder.itemView.findViewById(R.id.mapListItemImage)
            }
        })
    }*/

}

fun openAreaView(areaID: Int, context: Context) {
    val fragmentActivity = context as FragmentActivity
    val ft = fragmentActivity.supportFragmentManager.beginTransaction()
    //ft.addSharedElement(context.areaItemBackground, "areaLayout")
    //ft.addSharedElement(text, "areaViewName")
    Log.d("openAreaView", "id: $areaID")
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    val intent = AreaViewFragment()
    intent.arguments =
        Bundle().also { it.putInt("areaKey", areaID) }
    ft.add(R.id.exploreAreaReplacement, intent)
    ft.addToBackStack("areaView")
    ft.commit()
}
