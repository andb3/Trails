package com.andb.apps.trails.ui.area

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.andb.apps.trails.R
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiMap
import com.andb.apps.trails.data.model.SkiRegion
import com.andb.apps.trails.ui.common.MapItem
import com.andb.apps.trails.util.*
import com.github.rongi.klaster.Klaster
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.area_layout.*
import kotlinx.android.synthetic.main.offline_item.view.*
import org.koin.android.viewmodel.ext.android.viewModel


class AreaViewFragment : Fragment() {

    private val mapAdapter by lazy { mapAdapter() }
    private var skiArea: SkiArea? = null
    private var maps = listOf<SkiMap>()
    private var areaKey = -1

    private val viewModel: AreaViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        areaKey = arguments!!.getInt("areaKey")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.area_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapListRecycler.layoutManager = GridLayoutManager(context, 2)
        areaNested.apply {
            setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
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
        viewModel.loading.observe(viewLifecycleOwner, loadingObserver)
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
                viewModel.loadArea(areaKey)
            }
        }
    }

    private val mapsObserver = Observer<List<SkiMap>> { newMaps ->
        this.maps = newMaps.sortedByDescending { it.year }
        if (!maps.map { it.id }.equalsUnordered(skiArea?.maps ?: listOf())) {
            Log.d("areaFragmentOffline", "newMaps: ${maps.map { it.id }.sortedBy { it }}, area maps: ${skiArea?.maps?.sortedBy { it }}")
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

    private val loadingObserver = Observer<Boolean> { loading ->
        areaLoadingIndicator.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private val offlineObserver = Observer<Boolean> { offline->
        Log.d("offlineObserver", "offline: $offline")
        if (offline){
            setOffline{
                areaLoadingIndicator.visibility = View.VISIBLE
                viewModel.loadArea(areaKey)
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


    private fun setOffline(onRefresh: ((View) -> Unit)? = null) {
        areaLoadingIndicator.visibility = View.GONE
        areaOfflineItem.visibility = View.VISIBLE
        areaOfflineItem.offlineRefreshButton.setOnClickListener(onRefresh)
    }

    private fun setOnline() {
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
            (itemView as MapItem).apply {
                setup(maps[position], skiArea?.name ?: "")
                setOnFavoriteListener { map, favorite ->
                    viewModel.favoriteMap(map, favorite)
                }
            }
        }
        .build()


    companion object {
        const val BACKSTACK_TAG = "areaView"
    }
}

fun openAreaView(areaID: Int, context: Context) {
    time {
        val fragmentActivity = context as FragmentActivity

        val fragment = AreaViewFragment()
        fragment.putArguments {
            putInt("areaKey", areaID)
        }

        fragmentActivity.supportFragmentManager.commit {
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            add(R.id.exploreAreaReplacement, fragment)
            addToBackStack(AreaViewFragment.BACKSTACK_TAG)
        }
        Log.d("openAreaView", "id: $areaID")
    }

}
