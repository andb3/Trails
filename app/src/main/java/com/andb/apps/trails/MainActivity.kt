package com.andb.apps.trails

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.andb.apps.trails.pages.ExploreFragment
import com.andb.apps.trails.pages.FavoritesFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val favoritesFragment = FavoritesFragment()
    val exploreFragment = ExploreFragment()
    val searchFragment = SearchFragment()

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0 -> favoritesFragment

                1 -> exploreFragment

                2 -> searchFragment

                else -> null
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
        setSupportActionBar(toolbar)

        pager.adapter = SectionsPagerAdapter(supportFragmentManager)
        navigation.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(pager))


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.custom_area -> {
                val input = layoutInflater.inflate(R.layout.pick_area_popup, null)
                AlertDialog.Builder(this)
                        .setTitle("Area ID")
                        .setView(input)
                        .setPositiveButton("Load", DialogInterface.OnClickListener { dialog, which ->
                            //areaView.loadArea(input.pickAreaId.text.toString().toInt())
                        }).show()
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        if(RegionList.backStack.size>0 && pager.currentItem == 1){
            RegionList.drop()
            exploreFragment.exploreAdapter.notifyDataSetChanged()
        }else {
            super.onBackPressed()
        }

    }


}
