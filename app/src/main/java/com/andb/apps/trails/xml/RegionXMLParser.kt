package com.andb.apps.trails.xml

import android.util.Log
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.lang.Exception
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

object RegionXMLParser {
    fun parse(regionId: Int): SkiRegion?{

        val id: Int
        val name: String
        val mapCount: Int
        val children = ArrayList<SkiRegion>()
        val areas = ArrayList<SkiArea>()

        try {

            val url = URL("https://skimap.org/Regions/view/1.xml")
            val dbf = DocumentBuilderFactory.newInstance()
            val db = dbf.newDocumentBuilder()
            val doc = db.parse(InputSource(url.openStream()))
            doc.documentElement.normalize()

            val nodeList = doc.getElementsByTagName("region")

            for (i in 0 until nodeList.length) {

                val node = nodeList.item(i)
                val parent = node as Element
                id = parent.getAttribute("id").toInt()
                Log.d("region xml parsed", "ID: $id")

                val regionName = parent.getElementsByTagName("name").item(0) as Element
                name = regionName.textContent
                Log.d("area xml parsed", "Name: $name")

                val regionMaps = parent.getElementsByTagName("maps").item(0) as Element
                mapCount = regionMaps.getAttribute("count").toInt()
                Log.d("area xml parsed", "Map Count: $mapCount")




                val regionChildren = parent.getElementsByTagName("regions")
                for(r in 0 until  regionChildren.length){
                    val regionTag = regionChildren.item(r) as Element
                    val region = parse(regionTag.getAttribute("id").toInt())
                    if(region!=null) {
                        children.add(region)
                    }
                }
                Log.d("area xml parsed", "Regions: ${children.size}")


                val areaChildren = parent.getElementsByTagName("skiAreas")
                for(m in 0 until areaChildren.length){
                    val areaTag = areaChildren.item(m) as Element
                    val area = AreaXMLParser.parse(areaTag.getAttribute("id").toInt())
                    if(area!=null){
                        areas.add(area)
                    }
                }
                Log.d("area xml parsed", "Area Count: ${areas.size}")


                return SkiRegion(id, name, mapCount, areas, children)
            }


        }catch (e: Exception){
            Log.d("region xml parsing fail", "XML Parsing Exception = $e")
        }

        return null
    }
}