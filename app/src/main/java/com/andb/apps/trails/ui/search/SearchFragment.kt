package com.andb.apps.trails.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.andb.apps.trails.R
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.repository.AreasRepository
import com.andb.apps.trails.ui.common.AreaItem
import com.andb.apps.trails.util.newIoThread
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.search_layout.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class SearchFragment : Fragment() {

    var list = ArrayList<SkiArea>()
    val searchAdapter by lazy { searchAdapter() }
    val areasRepo: AreasRepository by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.search_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = "%$s%"
                CoroutineScope(Dispatchers.IO).launch {
                    list = if (!s.isNullOrEmpty()) ArrayList(areasRepo.search(searchText)) else ArrayList()
                    withContext(Dispatchers.Main) {
                        searchAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {}
        })

        searchClear.setOnClickListener {
            searchInput.setText("")
        }

    }

    private fun searchAdapter() = Klaster.get()
        .itemCount { list.size }
        .view { _, _ ->
            AreaItem(context ?: this.requireContext()).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
        .bind { position ->
            val area = list[position]
            (itemView as AreaItem).apply {
                setup(area)
                setOnFavoriteListener { area, favorite ->
                    newIoThread {
                        areasRepo.updateFavorite(area, favorite)
                    }
                }
            }

        }
        .build()
}