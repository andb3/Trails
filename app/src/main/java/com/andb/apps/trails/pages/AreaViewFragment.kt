package com.andb.apps.trails.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.trails.MapViewFragment
import com.andb.apps.trails.R
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.xml.AreaXMLParser
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.area_view.*
import kotlinx.android.synthetic.main.map_list_item.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main
import java.net.URL

class AreaViewFragment : Fragment() {

    var skiArea =
        SkiArea(0, "", 0, 0, 1970, URL("https://www.example.com"), ArrayList(), ArrayList())
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
        return inflater.inflate(R.layout.area_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapListRecycler.layoutManager = LinearLayoutManager(context)
        mapAdapter = mapAdapter()
        mapListRecycler.adapter = mapAdapter

        loadArea(areaKey)
    }

    fun loadArea(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val recieved = AreaXMLParser.parse(id)
            withContext(Dispatchers.Main) {

                skiArea = recieved

                skiArea.apply {
                    activity!!.toolbar.title = name
                    areaLiftCount.text = "Lifts: $liftCount"
                    areaRunCount.text = "Runs: $runCount"
                    areaOpeningYear.text = "Opening Year: $openingYear"
                    areaWebsite.text = "Website: $website"
                }
                mapAdapter.notifyDataSetChanged()
            }
        }
    }

    fun mapAdapter() = Klaster.get()
        .itemCount { skiArea.maps.size }
        .view(R.layout.map_list_item, layoutInflater)
        .bind { position ->
            mapListItemImage.setImageDrawable(skiArea.maps[position].image)
            mapListItemYear.text = skiArea.maps[position].year.toString()
            itemView.setOnClickListener {

                val activity = context as FragmentActivity
                val ft = activity.supportFragmentManager.beginTransaction()
                ft.addToBackStack("mapView")

                val fragment = MapViewFragment()
                val bundle = Bundle()
                bundle.putInt("mapKey", skiArea.maps[position].id)
                fragment.arguments = bundle

                ft.add(R.id.mapViewHolder, fragment)
                ft.commit()

            }
        }
        .build()
}