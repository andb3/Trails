package com.andb.apps.trails.pages

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.trails.R
import com.andb.apps.trails.RegionList
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.xml.RegionXMLParser
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.explore_item.*
import kotlinx.android.synthetic.main.explore_layout.*

class ExploreFragment : Fragment() {

    lateinit var exploreAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    var currentRegion: SkiRegion? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.explore_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val handler = Handler()
        Thread(Runnable {
            currentRegion = RegionXMLParser.parse(1)
            handler.post {
                exploreAdapter = exploreAdapter()
                exploreRegionRecycler.layoutManager = LinearLayoutManager(context)
                exploreRegionRecycler.adapter = exploreAdapter
            }
        }).start()

    }

    fun exploreAdapter() = Klaster.get()
            .itemCount { if(currentRegion!!.children.isEmpty()) currentRegion!!.areas.size else currentRegion!!.children.size }
            .view(R.layout.explore_item, layoutInflater)
            .bind { position ->
                currentRegion?.apply {
                    exploreRegionName.text = if(children.isEmpty()) areas[position].name else children[position].name
                    itemView.setOnClickListener {
                        if(children.isEmpty()) {
                            val fragmentActivity = context as FragmentActivity
                            val ft = fragmentActivity.supportFragmentManager.beginTransaction()

                            val intent = AreaViewFragment()
                            intent.arguments = Bundle().also { it.putInt("areaKey", areas[position].id) }
                            ft.add(R.id.exploreAreaReplacement, intent)
                            ft.commit()
                        }else {
                            currentRegion = children[position]
                        }
                    }
                }

            }

            .build()
}