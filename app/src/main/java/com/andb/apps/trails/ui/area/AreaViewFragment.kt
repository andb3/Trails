package com.andb.apps.trails.ui.area

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
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
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.effet.RippleEffect
import com.takusemba.spotlight.shape.RoundedRectangle
import jonathanfinerty.once.Once
import kotlinx.android.synthetic.main.area_layout.*
import kotlinx.android.synthetic.main.empty_item.view.*
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

        setupFab()
        setupScroll()
        setupOffline()
        setupEmpty()
        mapListRecycler.adapter = mapAdapter
        viewModel.skiArea.notNull().observe(viewLifecycleOwner, areaObserver)
        viewModel.maps.observe(viewLifecycleOwner, mapsObserver)
        viewModel.regions.observe(viewLifecycleOwner, regionsObserver)
        viewModel.loading.observe(viewLifecycleOwner, loadingObserver)
        viewModel.offline.observe(viewLifecycleOwner, offlineObserver)
        viewModel.emptyState.observe(viewLifecycleOwner, emptyObserver)
        viewModel.loadArea(areaKey)
    }

    private val areaObserver = Observer<SkiArea> { newArea ->
        this.skiArea = newArea
        areaViewName.text = newArea.name
        newArea.details?.apply {
            areaLiftCount.showIfAvailable(liftCount, R.string.area_lift_count_text)
            areaRunCount.showIfAvailable(runCount, R.string.area_run_count_text)
            areaOpeningYear.showIfAvailable(openingYear, R.string.area_opening_year_text)
            areaWebsite.showIfAvailable(website, R.string.area_website_text)
        }
    }

    private val mapsObserver = Observer<List<SkiMap>> { newMaps ->
        if (!newMaps.equalsUnordered(maps)) {
            this.maps = newMaps.sortedByDescending { it.year }
            areaLoadingIndicator.visibility = View.GONE
            mapAdapter.notifyDataSetChanged()
            mapListRecycler.scrollToPosition(0)
            mapListRecycler.scheduleLayoutAnimation()

            Log.d("spotlight", "maps.isNotEmpty() = ${maps.isNotEmpty()}")
            if (maps.isNotEmpty()) {
                doIfUndone("area_view_tooltip", Once.THIS_APP_INSTALL) {
                    mapListRecycler.doOnLayout {
                        Log.d("spotlight", "undone")
                        val targetView = mapListRecycler.getChildAt(0)
                        val spotlight = targetView.spotlight()
                        spotlight.start()
                    }
                }
            }
        }
    }

    private val regionsObserver = Observer<List<SkiRegion>> { regions ->
        val string = regions.joinToString { region -> region.name }
        areaViewRegions.text = String.format(getString(R.string.area_regions_text), string)
    }

    private val loadingObserver = Observer<Boolean> { loading ->
        areaLoadingIndicator.isVisible = loading
    }

    private val offlineObserver = Observer<Boolean> { offline ->
        areaOfflineItem.isVisible = offline
    }

    private val emptyObserver = Observer<Boolean> { empty ->
        areaEmptyItem.isVisible = empty
    }

    private fun View.spotlight(): Spotlight {
        val overlayView = layoutInflater.inflate(R.layout.area_view_tip_overlay, null)
        overlayView.updatePadding(top = this.getRectOnScreen().bottom + 16.dp)
        val target = Target.Builder()
            .setAnchor(this)
            .setShape(RoundedRectangle(height.toFloat(), width.toFloat(), 16.dp.toFloat()))
            .setEffect(
                RippleEffect(
                    50f, 200f,
                    ContextCompat.getColor(requireContext(), R.color.colorAccent)
                )
            )
            .setOverlay(overlayView)
            .build()

        val root = FrameLayout(requireContext())
        (rootView as ViewGroup).addView(root)

        val spotlight = Spotlight.Builder(requireActivity())
            .setTargets(target)
            .setBackgroundColor(R.color.iconDeselectedColor)
            .setDuration(200)
            .setAnimation(DecelerateInterpolator(2f))
            .setContainer(root)
            .setOnSpotlightListener(object : OnSpotlightListener {
                override fun onEnded() {
                    root.visibility = View.GONE
                }

                override fun onStarted() {}
            })
            .build()

        root.apply {
            isClickable = true
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    spotlight.finish()
                    val targetRect = this@spotlight.getRectOnScreen()
                    val clickingTarget = targetRect.contains(event.rawX.toInt(), event.rawY.toInt())
                    if (clickingTarget) {
                        this@spotlight.callOnClick()
                    }
                }
                return@setOnTouchListener true
            }
        }
        return spotlight
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

    private fun setupScroll() {
        areaNested.apply {
            setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
                if (scrollY > oldScrollY) {
                    areaViewFab?.hide()
                } else {
                    areaViewFab?.show()
                }
            }
        }
    }

    private fun setupOffline() {
        areaOfflineItem.offlineRefreshButton.setOnClickListener {
            viewModel.loadArea(areaKey)
        }
    }

    private fun setupEmpty() {
        areaEmptyItem.apply {
            emptyTitle.setText(R.string.empty_area_title)
            emptySummary.setText(R.string.empty_area_summary)
            emptyIcon.setImageResource(R.drawable.ic_terrain_black_24dp)
            emptyActionButton.apply {
                setIconResource(R.drawable.ic_language_black_24dp)
                setText(R.string.empty_area_action)
                setOnClickListener {
                    openURL(context, "https://skimap.org/pages/AddContent")
                }
            }
        }
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
