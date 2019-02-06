package com.andb.apps.trails.xml

import android.util.Log
import com.andb.apps.trails.database.Database
import com.andb.apps.trails.database.regionsDao
import com.andb.apps.trails.objects.BaseSkiRegion
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

object RegionXMLParser {

    suspend fun parseParent(regionId: Int) {
        parse(regionId, null)
    }


    suspend fun parse(regionId: Int, parentId: Int?): BaseSkiRegion {
        Log.d("regionDownload", "Downloading $regionId")
        Log.d("region xml parseFull", "parsing")

        //val children = ArrayList<SkiRegion>()
        //val areas = ArrayList<BaseSkiArea>()

        val node = getNode(regionId)


        val baseRegion: BaseSkiRegion = parseBase(node, parentId)
        regionsDao().insertRegion(baseRegion)


        val regionOrAreaChildren = node.getElementsByTagName("regions")

        if (regionOrAreaChildren.length > 0) {
            val jobs = mutableListOf<Job>()

            val regionChildren = (regionOrAreaChildren.item(0) as Element).childNodes
            for (r in 0 until regionChildren.length) {
                val job = CoroutineScope(Dispatchers.IO).launch {
                    val regionTag = regionChildren.item(r)
                    if (regionTag.nodeType == Node.ELEMENT_NODE) {
                        regionTag as Element
                        parse(regionTag.getAttribute("id").toInt(), baseRegion.id)
                    }
                }
                jobs.add(job)
            }

            jobs.forEach { it.join() }
        }

        return baseRegion
    }

    fun parseBase(node: Element, parentId: Int?): BaseSkiRegion {
        Log.d("base xml parseFull", "parsing")

        val id = node.getAttribute("id").toInt()
        Log.d("region xml parsed", "ID: $id")

        val regionName = node.getElementsByTagName("name").item(0) as Element
        val name = regionName.textContent
        Log.d("region xml parsed", "Name: $name")

        val regionMaps = node.getElementsByTagName("maps").item(0) as Element
        val mapCount = regionMaps.getAttribute("count").toInt()
        Log.d("region xml parsed", "SkiMap Count: $mapCount")

        return BaseSkiRegion(id, name, mapCount, parentId)

    }

    fun getNode(regionId: Int): Element {
        val url = URL("https://skimap.org/Regions/view/$regionId.xml")
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()

        val doc = db.parse(InputSource(url.openStream()))

        doc.documentElement.normalize()

        val nodeList = doc.getElementsByTagName("region")
        return nodeList.item(0) as Element
    }
}