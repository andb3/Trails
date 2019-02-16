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
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.database.regionAreaDao
import com.andb.apps.trails.objects.BaseSkiArea
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.utils.Utils
import com.andb.apps.trails.utils.dpToPx
import com.andb.apps.trails.views.items.MapItem
import com.andb.apps.trails.xml.AreaXMLParser
import com.github.rongi.klaster.Klaster
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.area_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main

class AreaViewFragment : Fragment() {

    lateinit var skiArea: SkiArea
    private val mapAdapter by lazy { mapAdapter() }
    private var areaKey = -1

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
        areaLoadingIndicator.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            skiArea = SkiArea(areasDao().getAreasById(id)[0], ArrayList())
            withContext(Dispatchers.Main) {
                skiArea.apply {
                    areaViewName.text = name
                    Utils.showIfAvailable(liftCount, areaLiftCount, R.string.area_lift_count_text)
                    Utils.showIfAvailable(runCount, areaRunCount, R.string.area_run_count_text)
                    Utils.showIfAvailable(openingYear, areaOpeningYear, R.string.area_opening_year_text)
                    Utils.showIfAvailable(website, areaWebsite, R.string.area_website_text)
                    CoroutineScope(Dispatchers.IO).launch {
                        val regions = regionsFromArea(skiArea)
                        withContext(Dispatchers.Main) {
                            areaViewRegions.text = String.format(getString(R.string.area_regions_text), regions)
                        }
                    }

                }
                mapListRecycler.adapter = mapAdapter
            }
            val received = AreaXMLParser.parseFull(id, skiArea)
            withContext(Dispatchers.Main) {
                areaLoadingIndicator?.visibility = View.GONE
                skiArea = received
                mapAdapter.notifyDataSetChanged()
            }
        }
    }


    private fun mapAdapter() = Klaster.get()
        .itemCount { skiArea.maps.size }
        .view { _, _ ->
            MapItem(context ?: this.requireContext()).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
        .bind { position ->
            (itemView as MapItem).setup(skiArea.maps[position])

        }
        .build()

    private fun regionsFromArea(area: BaseSkiArea): String {
        val regions = regionAreaDao().getRegionsForArea(area.id)
        return regions.joinToString { region -> region.name }

    }
}

fun openAreaView(area: BaseSkiArea, context: Context, text: View) {
    val fragmentActivity = context as FragmentActivity
    val ft = fragmentActivity.supportFragmentManager.beginTransaction()
    //ft.addSharedElement(context.areaItemBackground, "areaLayout")
    //ft.addSharedElement(text, "areaViewName")
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    val intent = AreaViewFragment()
    intent.arguments =
        Bundle().also { it.putInt("areaKey", area.id) }
    ft.add(R.id.exploreAreaReplacement, intent)
    ft.addToBackStack("areaView")
    ft.commit()
}
