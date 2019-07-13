package com.andb.apps.trails

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
import androidx.recyclerview.widget.GridLayoutManager
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.MapsRepo
import com.andb.apps.trails.repository.RegionsRepo
import com.andb.apps.trails.utils.*
import com.andb.apps.trails.views.MapItem
import com.github.rongi.klaster.Klaster
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.area_layout.*
import kotlinx.android.synthetic.main.offline_item.view.*

class AreaViewFragment : Fragment() {

    private val mapAdapter by lazy { mapAdapter() }
    private var maps = listOf<SkiMap>()
    private var areaKey = -1
    var skiArea: SkiArea? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        areaKey = arguments!!.getInt("areaKey")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.area_layout, container, false)
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
        loadArea(areaKey)
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

    private fun loadArea(id: Int) {
        setOnline()
        newIoThread {
            val skiArea = AreasRepo.getAreaById(id)
            if(this@AreaViewFragment.isVisible) { //check for if user navigated out of this fragment before load
                mainThread {
                    if (skiArea != null) {
                        areaViewName.text = skiArea.name
                        skiArea.details.apply {
                            areaLiftCount.showIfAvailable(liftCount, R.string.area_lift_count_text)
                            areaRunCount.showIfAvailable(runCount, R.string.area_run_count_text)
                            areaOpeningYear.showIfAvailable(openingYear, R.string.area_opening_year_text)
                            areaWebsite.showIfAvailable(website, R.string.area_website_text)
                        }
                        ioThread {
                            val regions = regionsFromArea(skiArea)
                            mainThread{
                                if(this@AreaViewFragment.isVisible) {
                                    areaViewRegions.text = String.format(getString(R.string.area_regions_text), regions)
                                }
                            }
                        }

                        ioThread {
                            maps = MapsRepo.getMapsNonLive(skiArea).sortedByDescending { it.year }
                            mainThread {
                                if (this@AreaViewFragment.isVisible) {
                                    areaLoadingIndicator.visibility = View.GONE
                                    mapAdapter.notifyDataSetChanged()
                                    mapListRecycler.scrollToPosition(0)
                                    mapListRecycler.scheduleLayoutAnimation()
                                    if(maps.map { it.id } != skiArea.maps){
                                        setOffline {
                                            loadArea(id)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        setOffline {
                            loadArea(id)
                        }
                    }


                }
            }
            this@AreaViewFragment.skiArea = skiArea
        }
    }

    fun setOffline(onRefresh: ((View) -> Unit)? = null){
/*        areaViewFab.visibility = View.GONE
        if (isTranslated()) {
            toggleInfo(areaViewFab)
        }*/
        areaLoadingIndicator.visibility = View.GONE
        areaOfflineItem.visibility = View.VISIBLE
        areaOfflineItem.offlineRefreshButton.setOnClickListener(onRefresh)
    }

    fun setOnline() {
        areaViewName.text = ""
        areaViewFab.visibility = View.VISIBLE
        areaLoadingIndicator.visibility = View.VISIBLE
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
            (itemView as MapItem).setup(maps[position], skiArea?.name?:"")
        }
        .build()

    private suspend fun regionsFromArea(area: SkiArea): String {
        val regions = RegionsRepo.findAreaParents(area)
        return regions.joinToString { region -> region.name }
    }
}

fun openAreaView(areaId: Int, context: Context) {
    val fragmentActivity = context as FragmentActivity
    val ft = fragmentActivity.supportFragmentManager.beginTransaction()
    //ft.addSharedElement(context.areaItemBackground, "areaLayout")
    //ft.addSharedElement(text, "areaViewName")
    Log.d("openAreaView", "id: $areaId")
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    val intent = AreaViewFragment()
    intent.arguments =
        Bundle().also { it.putInt("areaKey", areaId) }
    ft.add(R.id.exploreAreaReplacement, intent)
    ft.addToBackStack("areaView")
    ft.commit()
}
