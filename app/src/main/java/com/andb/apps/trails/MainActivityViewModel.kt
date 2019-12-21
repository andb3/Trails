package com.andb.apps.trails

import androidx.lifecycle.ViewModel
import com.andb.apps.trails.ui.explore.ExploreFragment
import com.andb.apps.trails.ui.favorites.FavoritesFragment
import com.andb.apps.trails.ui.search.SearchFragment

class MainActivityViewModel(val favoritesFragment: FavoritesFragment,
                            val exploreFragment: ExploreFragment,
                            val searchFragment: SearchFragment) : ViewModel() {
    var pagerPosition: Int = 0
}