package com.andb.apps.trails

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.andb.apps.trails.objects.Map
import com.andb.apps.trails.xml.AreaXMLParser
import com.andb.apps.trails.xml.MapXMLParser
import kotlinx.android.synthetic.main.area_view.*
import kotlinx.android.synthetic.main.map_view.*

class MapViewFragment : Fragment() {

    var map: Map? = null
    var mapKey = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapKey = arguments!!.getInt("mapKey")
        Log.d("inititalized fragment", "mapKey: $mapKey")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.map_view, container!!.parent as ViewGroup, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("MapViewFragment", "before thread")
        val handler = Handler()
        Thread(Runnable {
            Log.d("MapViewFragment", "before parsing")
            map = MapXMLParser.parse(mapKey)
            Log.d("MapViewFragment", "after parsing")
            handler.post {
                map?.apply {
                    skiMapAreaName?.text = skiArea.name
                    skiMapYear?.text = year.toString()
                    skiMapImage?.apply {
                        setImageDrawable(image)
                        setColorFilter(Color.argb(0, 0, 0, 0))
                    }
                }
            }
        }).start()

    }
}