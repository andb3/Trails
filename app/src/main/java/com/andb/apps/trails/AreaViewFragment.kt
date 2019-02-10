package com.andb.apps.trails

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.utils.Utils
import com.andb.apps.trails.utils.dpToPx
import com.andb.apps.trails.views.items.MapItem
import com.andb.apps.trails.xml.AreaXMLParser
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.area_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main

class AreaViewFragment : Fragment() {

    lateinit var skiArea: SkiArea
    lateinit var mapAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    var areaKey = -1

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

        loadArea(areaKey)
    }

    fun setupFab() {
        areaViewFab.apply {
            imageTintList = ColorStateList.valueOf(Color.WHITE)
            setOnClickListener {
                TransitionManager.beginDelayedTransition(areaLayout, ChangeBounds())
                val translated = areaInfoCard.translationY != 0f
                val translateCard: Float
                val translateFab: Float
                val alpha: Float
                if (!translated) {
                    translateCard = -areaInfoCard.height - dpToPx(8).toFloat()
                    translateFab = -areaInfoCard.height + dpToPx(8).toFloat()
                    alpha = .7f
                    setImageDrawable(resources.getDrawable(R.drawable.ic_expand_more_black_24dp))
                    areaViewDim.visibility = View.VISIBLE
                } else {
                    translateCard = 0f
                    translateFab = 0f
                    alpha = 0f
                    setImageDrawable(resources.getDrawable(R.drawable.ic_info_outline_black_24dp))
                    areaViewDim.visibility = View.GONE
                }
                areaViewDim.animate().alpha(alpha)
                areaInfoCard.animate().translationY(translateCard)
                this.animate().translationY(translateFab)
            }
        }

    }

    fun loadArea(id: Int) {
        activity!!.loadingIndicator.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            skiArea = SkiArea(areasDao().getAreasById(id)[0], ArrayList())
            withContext(Dispatchers.Main) {
                skiArea.apply {
                    areaViewName.text = name
                    Utils.showIfAvailible(liftCount, areaLiftCount, R.string.area_lift_count_text)
                    Utils.showIfAvailible(runCount, areaRunCount, R.string.area_run_count_text)
                    Utils.showIfAvailible(openingYear, areaOpeningYear, R.string.area_opening_year_text)
                    Utils.showIfAvailible(website, areaWebsite, R.string.area_website_text)
                }
                mapAdapter = mapAdapter()
                mapListRecycler.adapter = mapAdapter
            }
            val recieved = AreaXMLParser.parseFull(id, skiArea)
            withContext(Dispatchers.Main) {
                activity!!.loadingIndicator.visibility = View.GONE
                skiArea = recieved
                mapAdapter.notifyDataSetChanged()
            }
        }
    }


    fun mapAdapter() = Klaster.get()
        .itemCount { skiArea.maps.size }
        .view { _, _ ->
            MapItem(context?: this.requireContext()).also {
                it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
        .bind { position ->
            (itemView as MapItem).setup(skiArea.maps[position])

        }
        .build()
}
