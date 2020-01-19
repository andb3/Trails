package com.andb.apps.trails.ui.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andb.apps.trails.R
import com.andb.apps.trails.data.local.MapsDao
import com.andb.apps.trails.data.model.isPdf
import com.andb.apps.trails.util.mainThread
import com.andb.apps.trails.util.newIoThread
import kotlinx.android.synthetic.main.test_activity.*
import org.koin.android.ext.android.inject

class TestActivity : AppCompatActivity(){

    //val changeImageTransform = TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform)
    val mapsDao: MapsDao by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)
        mapRepoTest()
    }

    fun mapRepoTest() {
        newIoThread {
            val allMaps = mapsDao.getAllStatic()
            val mapsCount = allMaps.size
            val pdfCount = allMaps.filter { it.url.isPdf() }.size
            mainThread {
                testTextView.text = "PDF Count: $pdfCount"
            }
        }
    }

    fun mapTransition() {
        /*
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
        })*/
    }

}