package com.andb.apps.trails.ui.explore

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import androidx.core.view.postDelayed
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.andb.apps.trails.R
import com.andb.apps.trails.data.local.Prefs
import com.andb.apps.trails.data.model.SkiRegionTree
import com.andb.apps.trails.data.remote.Updater
import com.andb.apps.trails.ui.area.openAreaView
import com.andb.apps.trails.util.newIoThread
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller
import kotlinx.android.synthetic.main.explore_layout.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        exploreRegionRecycler.addItemDecoration(ExploreItemDecoration())

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
        exploreRecyclerFastScroller.handleView.updatePadding(right = 0)
    }

    private val onTreeChangeListener = Observer<SkiRegionTree> { region ->
        //Log.d("onTreeChangeListener", "new parent: $region")
        exploreAdapter.updateTree(region)
        exploreRecyclerFastScroller.isFastScrollEnabled = !viewModel.isBaseRegion()
        Log.d("animateTest", "animating visibility to ${!viewModel.isBaseRegion()}")
        //exploreRecyclerFastScroller.handleView.visibility = if(!viewModel.isBaseRegion()) View.VISIBLE else View.GONE
        exploreRecyclerFastScroller.handleView.animateVisibility(!viewModel.isBaseRegion())
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

val RecyclerViewFastScroller.handleView: ImageView
    get() = findViewById<ImageView>(com.qtalk.recyclerviewfastscroller.R.id.thumbIV)

private fun View.animateVisibility(makeVisible: Boolean, duration: Long = 150) {
    val scaleFactor: Float = if (makeVisible) 1f else 0f
    Log.d("animateVisibility", "showing: $makeVisible")
    if(makeVisible) visibility = View.VISIBLE
    CoroutineScope(Dispatchers.Main).launch {
        animate().scaleY(scaleFactor).setDuration(duration).start()
        postDelayed(duration) {
            Log.d(
                "animateVisibility",
                "hiding: ${!makeVisible}"
            ); if (!makeVisible) visibility = View.GONE
        }
    }
}

private inline fun ViewPropertyAnimator.onAnimationCancelled(crossinline body: () -> Unit){
    this.setListener(object : Animator.AnimatorListener{
        override fun onAnimationRepeat(p0: Animator?) {}
        override fun onAnimationEnd(p0: Animator?) {}
        override fun onAnimationStart(p0: Animator?) {}
        override fun onAnimationCancel(p0: Animator?) { body() }
    })
}
private inline fun ViewPropertyAnimator.onAnimationEnd(crossinline body: () -> Unit):ViewPropertyAnimator{
    this.setListener(object : Animator.AnimatorListener{
        override fun onAnimationRepeat(p0: Animator?) {}
        override fun onAnimationEnd(p0: Animator?) {
            body()
        }
        override fun onAnimationStart(p0: Animator?) {}
        override fun onAnimationCancel(p0: Animator?) {}
    })
    return this
}

