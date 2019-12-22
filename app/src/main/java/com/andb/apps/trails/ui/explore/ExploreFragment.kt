package com.andb.apps.trails.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.andb.apps.trails.R
import com.andb.apps.trails.data.local.Prefs
import com.andb.apps.trails.data.model.SkiRegionTree
import com.andb.apps.trails.data.remote.Updater
import com.andb.apps.trails.ui.area.openAreaView
import com.andb.apps.trails.util.newIoThread
import kotlinx.android.synthetic.main.explore_layout.*
import org.koin.android.viewmodel.ext.android.viewModel


class ExploreFragment : Fragment() {

    private val exploreAdapter = ExploreAdapter()
    val viewModel: ExploreViewModel by viewModel()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.explore_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exploreRegionRecycler.layoutManager = LinearLayoutManager(context)
        exploreRegionRecycler.adapter = exploreAdapter
        exploreRegionRecycler.setHasFixedSize(true)

        exploreAdapter.setOnRegionClickListener { viewModel.nextRegion(it.id) }
        exploreAdapter.setOnAreaClickListener { openAreaView(it.id, requireContext()) }
        exploreAdapter.setOnAreaFavoriteListener { area, favorite -> viewModel.updateFavorite(area, favorite) }
        exploreAdapter.setOnHeaderSwitchListener { viewModel.setBaseRegion(it) }
        exploreAdapter.setOnOfflineRefreshListener {
            newIoThread {
                Updater.updateRegions()
                Updater.updateAreas()
                viewModel.refresh()
            }
        }

        if (viewModel.isFirstLoad()) {
            viewModel.setup(Prefs.startingRegion)
        }

        viewModel.tree.observe(viewLifecycleOwner, onTreeChangeListener)
        viewModel.loading.observe(viewLifecycleOwner, onLoadingChangeListener)
        viewModel.offline.observe(viewLifecycleOwner, onOfflineChangeListener)

    }

    private val onTreeChangeListener = Observer<SkiRegionTree> { region ->
        //Log.d("onTreeChangeListener", "new parent: $region")
        exploreAdapter.updateTree(region)
    }



    private val onOfflineChangeListener = Observer<Boolean> { offline ->
        if (offline) {
            exploreAdapter.notifyItemInserted(exploreAdapter.itemCount - 1)
        } else {
            exploreAdapter.notifyItemRemoved(exploreAdapter.itemCount)
        }
    }

    private val onLoadingChangeListener = Observer<Boolean> { loading ->
        if (loading) {
            exploreLoadingIndicator.visibility = View.VISIBLE
        } else {
            exploreLoadingIndicator.visibility = View.GONE
        }
    }
}

