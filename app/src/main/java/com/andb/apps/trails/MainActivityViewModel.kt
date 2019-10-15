package com.andb.apps.trails

import androidx.lifecycle.ViewModel
import com.andb.apps.trails.pages.ExploreFragment
import com.andb.apps.trails.pages.FavoritesFragment
import com.andb.apps.trails.pages.SearchFragment

class MainActivityViewModel(val favoritesFragment: FavoritesFragment,
                            val exploreFragment: ExploreFragment,
                            val searchFragment: SearchFragment) : ViewModel() {
    var pagerPosition: Int = 0
}