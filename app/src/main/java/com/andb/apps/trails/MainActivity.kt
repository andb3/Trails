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
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.MapsRepo
import com.andb.apps.trails.repository.RegionsRepo
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val viewModel by lazy { ViewModelProviders.of(this).get(MainActivityViewModel::class.java) }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) :
        FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> viewModel.favoritesFragment

                1 -> viewModel.exploreFragment

                else -> viewModel.searchFragment
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

        RegionsRepo.init(this)
        AreasRepo.init(this)
        MapsRepo.init(this)

        navigation.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(pager))
        setAdapter(pager)
    }


    private fun setAdapter(pager: ViewPager) {
        pager.adapter = SectionsPagerAdapter(supportFragmentManager)
        navigation.selectTab(navigation.getTabAt(viewModel.pagerPosition))
        navigation.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.pagerPosition = tab?.position ?: 0
            }
        })

    }


    override fun onBackPressed() {

        setStatusBarColors(this)
        if (supportFragmentManager.backStackEntryCount > 0) {
            super.onBackPressed()
        } else if (viewModel.exploreFragment.viewModel.isBackPossible() && pager.currentItem == 1) {
            viewModel.exploreFragment.viewModel.backRegion()
        } else {
            super.onBackPressed()
        }

    }

}
