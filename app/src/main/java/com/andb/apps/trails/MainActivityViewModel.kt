package com.andb.apps.trails

import androidx.lifecycle.ViewModel
import com.andb.apps.trails.pages.ExploreFragment
import com.andb.apps.trails.pages.FavoritesFragment
import com.andb.apps.trails.pages.SearchFragment

class MainActivityViewModel : ViewModel(){
    val favoritesFragment by lazy { FavoritesFragment() }
    val exploreFragment by lazy { ExploreFragment() }
    val searchFragment by lazy { SearchFragment() }

    var pagerPosition: Int = 0
}