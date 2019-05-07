package com.andb.apps.trails

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.pages.ExploreFragment
import com.andb.apps.trails.pages.FavoritesFragment
import com.andb.apps.trails.pages.SearchFragment
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.MapsRepo
import com.andb.apps.trails.repository.RegionsRepo
import com.andb.apps.trails.utils.dropBy
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.explore_layout.*

class MainActivity : AppCompatActivity() {

    val favoritesFragment by lazy { FavoritesFragment() }
    val exploreFragment by lazy { ExploreFragment() }
    val searchFragment by lazy { SearchFragment() }

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
/*
        RegionsRepo.init(this)
        AreasRepo.init(this)
        MapsRepo.init(this)*/

        navigation.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(pager))
        setAdapter(pager)
    }


    private fun setAdapter(pager: ViewPager) {
        pager.adapter = SectionsPagerAdapter(supportFragmentManager)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.test_menu_item -> {
                AsyncTask.execute {
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
        } else if (exploreFragment.regionStack.size > 1 && pager.currentItem == 1) {
            exploreFragment.regionStack.dropBy(1)
            exploreFragment.backRegion()
        } else {
            super.onBackPressed()
        }

    }

}
