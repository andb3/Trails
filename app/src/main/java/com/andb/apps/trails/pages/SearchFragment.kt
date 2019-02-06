package com.andb.apps.trails.pages

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.andb.apps.trails.AreaViewFragment
import com.andb.apps.trails.MapViewFragment
import com.andb.apps.trails.R
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.lists.FavoritesList
import com.andb.apps.trails.objects.BaseSkiArea
import com.andb.apps.trails.utils.Utils
import com.andb.apps.trails.xml.AreaXMLParser
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.favorites_area_item.*
import kotlinx.android.synthetic.main.search_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main

class SearchFragment : Fragment() {

    var list = ArrayList<BaseSkiArea>()
    val searchAdapter by lazy { searchAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.search_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }

        searchInput.addTextChangedListener(object : TextWatcher{
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = "%$s%"
                CoroutineScope(Dispatchers.IO).launch {
                    list = if(!s.isNullOrEmpty()) ArrayList(areasDao().search(searchText)) else ArrayList()
                    withContext(Dispatchers.Main) {
                        searchAdapter.notifyDataSetChanged()
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun afterTextChanged(s: Editable) {}
        })

    }



    fun searchAdapter() = Klaster.get()
        .itemCount { list.size }
        .view(R.layout.favorites_area_item, layoutInflater)
        .bind {position ->
            val area = list[position]
            favoritesAreaItemName.text = area.name
            Utils.showIfAvailible(area.liftCount, favoritesAreaItemLifts, R.string.area_lift_count_text)
            Utils.showIfAvailible(area.runCount, favoritesAreaItemRuns, R.string.area_run_count_text)
            favoritesAreaCurrentMap.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val mapKey = AreaXMLParser.parseFull(area.id).maps[0].id
                    withContext(Dispatchers.Main){
                        val activity = context as FragmentActivity
                        val ft = activity.supportFragmentManager.beginTransaction()
                        ft.addToBackStack("mapView")

                        val fragment = MapViewFragment()
                        val bundle = Bundle()
                        bundle.putInt("mapKey", mapKey)
                        fragment.arguments = bundle

                        ft.add(R.id.mapViewHolder, fragment)
                        ft.commit()
                    }
                }

            }
            itemView.setOnClickListener {
                val fragmentActivity = context as FragmentActivity
                val ft = fragmentActivity.supportFragmentManager.beginTransaction()

                val intent = AreaViewFragment()
                intent.arguments =
                    Bundle().also { it.putInt("areaKey", area.id) }
                ft.add(R.id.exploreAreaReplacement, intent)
                ft.addToBackStack("areaView")
                ft.commit()
            }

        }
        .build()
}