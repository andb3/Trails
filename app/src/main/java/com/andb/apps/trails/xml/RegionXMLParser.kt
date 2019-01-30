package com.andb.apps.trails.xml

import android.util.Log
import com.andb.apps.trails.objects.BaseSkiArea
import com.andb.apps.trails.objects.BaseSkiRegion
import com.andb.apps.trails.objects.SkiRegion
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

object RegionXMLParser {
    suspend fun parse(regionId: Int): SkiRegion {

        Log.d("region xml parse", "parsing")

        val id: Int
        val name: String
        val mapCount: Int
        val children = ArrayList<BaseSkiRegion>()
        val areas = ArrayList<BaseSkiArea>()


        val url = URL("https://skimap.org/Regions/view/$regionId.xml")
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()

        val doc = db.parse(InputSource(url.openStream()))

        doc.documentElement.normalize()

        val nodeList = doc.getElementsByTagName("region")
        val node = nodeList.item(0)
        val parent = node as Element

        id = parent.getAttribute("id").toInt()
        Log.d("region xml parsed", "ID: $id")

        val regionName = parent.getElementsByTagName("name").item(0) as Element
        name = regionName.textContent
        Log.d("region xml parsed", "Name: $name")

        val regionMaps = parent.getElementsByTagName("maps").item(0) as Element
        mapCount = regionMaps.getAttribute("count").toInt()
        Log.d("region xml parsed", "Map Count: $mapCount")

        val regionOrAreaChildren = parent.getElementsByTagName("regions")

        if (regionOrAreaChildren.length > 0) {
            val jobs = mutableListOf<Deferred<BaseSkiRegion?>>()

            val regionChildren = (regionOrAreaChildren.item(0) as Element).childNodes
            for (r in 0 until regionChildren.length) {
                val regionJob = CoroutineScope(Dispatchers.IO).async {
                    val regionTag = regionChildren.item(r)
                    if (regionTag.nodeType == Node.ELEMENT_NODE) {
                        regionTag as Element
                        return@async parseBase(regionTag.getAttribute("id").toInt())
                    }

                    return@async null
                }

                jobs.add(regionJob)
            }
            jobs.forEach {
                val region = it.await()
                if (region != null && region.mapCount > 0) {
                    children.add(region)
                }
                it.join()

            }

        } else {
            val jobs = mutableListOf<Deferred<BaseSkiArea>>()

            val areaChildren = parent.getElementsByTagName("skiArea")
            for (m in 0 until areaChildren.length) {
                val regionJob = CoroutineScope(Dispatchers.IO).async {
                    val areaTag = areaChildren.item(m) as Element
                    return@async AreaXMLParser.parseBase(areaTag.getAttribute("id").toInt())
                }

                jobs.add(regionJob)

            }

            jobs.forEach {
                val area = it.await()

                areas.add(area)

                it.join()

            }
        }




        Log.d("region xml parsed", "Regions: ${children.size}")
        Log.d("area xml parsed", "Area Count: ${areas.size}")
        return SkiRegion(id, name, mapCount, areas, children)


    }

    fun parseBase(regionId: Int): BaseSkiRegion {
        Log.d("base xml parse", "parsing")

        val id: Int
        val name: String
        val mapCount: Int


        val url = URL("https://skimap.org/Regions/view/$regionId.xml")
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()

        val doc = db.parse(InputSource(url.openStream()))

        doc.documentElement.normalize()

        val nodeList = doc.getElementsByTagName("region")


        val node = nodeList.item(0)
        val parent = node as Element
        id = parent.getAttribute("id").toInt()
        Log.d("region xml parsed", "ID: $id")

        val regionName = parent.getElementsByTagName("name").item(0) as Element
        name = regionName.textContent
        Log.d("region xml parsed", "Name: $name")

        val regionMaps = parent.getElementsByTagName("maps").item(0) as Element
        mapCount = regionMaps.getAttribute("count").toInt()
        Log.d("region xml parsed", "Map Count: $mapCount")

        return BaseSkiRegion(id, name, mapCount)

    }
}