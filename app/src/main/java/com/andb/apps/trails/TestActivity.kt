package com.andb.apps.trails

import android.os.Bundle
import android.text.TextUtils.replace
import android.transition.TransitionInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.commit
import com.andb.apps.trails.utils.GlideApp
import kotlinx.android.synthetic.main.map_item.*

class TestActivity : AppCompatActivity(){

    //val changeImageTransform = TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)
        mapFavoritesAreaName.text = "Steamboat"
        mapListItemYear.text = "2019"
        mapListItemImage.transitionName = "transitionTest"
        GlideApp.with(this).load("https://skimap.org/data/500/3100/1548642791thumb.jpg").into(mapListItemImage)
        mapListItemImage.setOnClickListener {
            //openMapView(14834, this, mapListItemImage)
            val fragment = MapViewFragment()
            val bundle = Bundle()
            bundle.putInt("mapKey", 14834)
            bundle.putString("transitionName", mapListItemImage.transitionName)
            fragment.arguments = bundle


            supportFragmentManager.commit {
                setReorderingAllowed(true)
                addToBackStack("mapView")
                addSharedElement(mapListItemImage, mapListItemImage.transitionName)
                //setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                replace(R.id.mapViewHolder, fragment)
            }
        }
        //exitTransition = TransitionInflater.from(this).inflateTransition(R.transition.image_shared_element_transition)
        setExitSharedElementCallback(object : SharedElementCallback(){
            override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
                sharedElements!!.put(names!![0], mapListItemImage)
            }
        })
    }

}