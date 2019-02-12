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
import com.andb.apps.trails.views.items.AreaItem
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
        .view { _, _ ->
            AreaItem(context?: this.requireContext()).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
        .bind {position ->
            val area = list[position]
            (itemView as AreaItem).setup(area)

        }
        .build()
}