package com.andb.apps.trails

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.database.db
import com.andb.apps.trails.database.regionAreaDao
import com.andb.apps.trails.download.setupRegions
import com.andb.apps.trails.download.updateAreas
import com.andb.apps.trails.lists.AreaList
import com.andb.apps.trails.lists.FavoritesList
import com.andb.apps.trails.lists.RegionList
import com.andb.apps.trails.pages.ExploreFragment
import com.andb.apps.trails.pages.FavoritesFragment
import com.andb.apps.trails.pages.SearchFragment
import com.google.android.material.tabs.TabLayout
import jonathanfinerty.once.Once
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.explore_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {

    val favoritesFragment by lazy { FavoritesFragment() }
    val exploreFragment  by lazy { ExploreFragment() }
    val searchFragment  by lazy { SearchFragment() }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) :
        FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> favoritesFragment

                1 -> exploreFragment

                else -> searchFragment
            }
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        CoroutineScope(Dispatchers.IO).launch {
            setupData(pager)
            AreaList.setup()
        }
        navigation.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(pager))/* {
            when (it.itemId){
                R.id.navigation_fav -> pager.currentItem = 0

                R.id.navigation_explore -> pager.currentItem = 1

                R.id.navigation_search -> pager.currentItem = 2
            }

            true
        }*/


    }

    lateinit var dialog: ProgressDialog
    suspend fun setupData(pager: ViewPager){

        if(!Once.beenDone(TAG_REGION_SETUP) && !Once.beenDone(TAG_AREA_SETUP)){
            withContext(Dispatchers.Main) {
                dialog = ProgressDialog.show(this@MainActivity, notifTitle(this@MainActivity, 0), getString(R.string.download_progress_desc), false, false)
            }

            startService(Intent(this, InitialDownloadService::class.java))
        }else{
            pager.adapter = SectionsPagerAdapter(supportFragmentManager)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.test_menu_item -> {
                AsyncTask.execute {
                    Log.d("dbCount", "Join Count: ${regionAreaDao().getSize()}")
                    Log.d("dbCount", "Area Count: ${areasDao().getSize()}")
                }
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        setStatusBarColors(this)
        if (supportFragmentManager.backStackEntryCount > 0) {
            super.onBackPressed()
        } else if (RegionList.backStack.size > 1 && pager.currentItem == 1) {
            RegionList.drop()
            exploreFragment.exploreAdapter.notifyDataSetChanged()
            exploreRegionRecycler.scrollToPosition(0)
            exploreRegionRecycler.scheduleLayoutAnimation()
        } else {
            super.onBackPressed()
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun downloadCallback(event: DownloadEvent){
        when(event.status){
            -1->{
                dialog.progress = event.progress
            }
            DOWNLOADING_REGIONS->{
                dialog.setTitle(notifTitle(this, event.status))
                dialog.progress = event.progress
            }
            DOWNLOADING_AREAS->{
                dialog.setTitle(notifTitle(this, event.status))
                dialog.progress = event.progress
            }
            DOWNLOADING_SUCEEDED->{
                dialog.cancel()
                pager.adapter = SectionsPagerAdapter(supportFragmentManager)
            }
            DOWNLOADING_FAILED->{
                dialog.cancel()
                AlertDialog.Builder(this).setTitle(R.string.download_progress_failed).setPositiveButton(R.string.download_progress_try_again){ dlg, _ ->
                    dlg.cancel()
                    CoroutineScope(Dispatchers.IO).launch {
                        setupData(pager)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


}
