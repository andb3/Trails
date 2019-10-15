package com.andb.apps.trails

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    val viewModel: MainActivityViewModel by viewModel()

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

        if (viewModel.exploreFragment.isAdded) {
            //viewModel.exploreFragment.viewModel.setBaseRegion(1)
        }
        viewModel.favoritesFragment.refresh(viewModel.favoritesFragment.isAdded)

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
        } else if (viewModel.exploreFragment.isBackPossible() && pager.currentItem == 1) {
            //viewModel.exploreFragment.viewModel.backRegion()
            viewModel.exploreFragment.backRegion()
        } else {
            super.onBackPressed()
        }

    }

}
