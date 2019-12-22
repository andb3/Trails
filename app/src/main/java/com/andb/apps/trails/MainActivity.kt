package com.andb.apps.trails

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.andb.apps.trails.ui.map.MapViewFragment
import com.andb.apps.trails.ui.settings.SettingsFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.get
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    val viewModel: MainActivityViewModel by viewModel()

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> viewModel.favoritesFragment
                1 -> viewModel.exploreFragment
                else -> viewModel.searchFragment
            }
        }

        override fun getCount(): Int = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        Log.d("onBackPressed", "backStackCount: ${supportFragmentManager.backStackEntryCount}")
        when {
            supportFragmentManager.backStackEntryCount > 0 -> {
                val settingsFragment: SettingsFragment = get()
                when {
                    settingsFragment.isAdded && settingsFragment.canGoBack() -> settingsFragment.goBack()
                    supportFragmentManager.findFragmentByTag(MapViewFragment.BACKSTACK_TAG) != null -> {
                        super.onBackPressed()
                        window.addFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    }
                    else -> super.onBackPressed()
                }
            }
            viewModel.exploreFragment.viewModel.isBackPossible() && pager.currentItem == 1 -> {
                Log.d("onBackPressed", "backRegion")
                viewModel.exploreFragment.viewModel.backRegion()
            }
            else -> super.onBackPressed()
        }
    }
}
