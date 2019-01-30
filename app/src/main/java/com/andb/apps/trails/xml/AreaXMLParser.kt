package com.andb.apps.trails.xml

import android.util.Log
import com.andb.apps.trails.objects.BaseSkiArea
import com.andb.apps.trails.objects.BaseSkiRegion
import com.andb.apps.trails.objects.Map
import com.andb.apps.trails.objects.SkiArea
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

object AreaXMLParser {
    suspend fun parse(areaId: Int): SkiArea {

        val id: Int
        val name: String
        val liftCount: Int
        val runCount: Int
        val openingYear: Int
        val website: URL
        val regions = ArrayList<BaseSkiRegion>()
        val maps = ArrayList<Map>()


        val url = URL("https://skimap.org/SkiAreas/view/$areaId.xml")
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(InputSource(url.openStream()))
        doc.documentElement.normalize()

        val nodeList = doc.getElementsByTagName("skiArea")


        val node = nodeList.item(0)
        val parent = node as Element
        id = parent.getAttribute("id").toInt()
        Log.d("area xml parsed", "ID: $id")

        val skiAreaName = parent.getElementsByTagName("name").item(0) as Element
        name = skiAreaName.textContent
        Log.d("area xml parsed", "Name: $name")

        val skiAreaLiftCount = parent.getElementsByTagName("liftCount").item(0) as Element?
        liftCount = skiAreaLiftCount?.textContent?.toInt() ?: -1

        Log.d("area xml parsed", "Lift Count: $liftCount")

        val skiAreaRunCount = parent.getElementsByTagName("runCount").item(0) as Element?
        runCount = skiAreaRunCount?.textContent?.toInt() ?: -1

        Log.d("area xml parsed", "Run Count: $runCount")

        val skiAreaOpeningYear =
            parent.getElementsByTagName("openingYear").item(0) as Element?
        openingYear = skiAreaOpeningYear?.textContent?.toInt() ?: -1
        Log.d("area xml parsed", "Opening Year: $openingYear")


        val skiAreaWebsite =
            parent.getElementsByTagName("officialWebsite").item(0) as Element?
        website = URL(skiAreaWebsite?.textContent ?: "https://www.example.com")
        Log.d("area xml parsed", "Website: $website")


        val skiAreaRegions = parent.getElementsByTagName("region")
        for (r in 0 until skiAreaRegions.length) {
            val regionTag = skiAreaRegions.item(r) as Element
            val region =
                BaseSkiRegion(regionTag.getAttribute("id").toInt(), regionTag.textContent)
            regions.add(region)
        }
        Log.d("area xml parsed", "Regions: ${regions.size}")

        val jobs = mutableListOf<Deferred<Map?>>()

        val skiAreaMaps = parent.getElementsByTagName("skiMap")
        for (m in 0 until skiAreaMaps.length) {
            val job = CoroutineScope(Dispatchers.IO).async {
                val mapTag = skiAreaMaps.item(m) as Element
                return@async MapXMLParser.parse(mapTag.getAttribute("id").toInt(), true)
            }


            jobs.add(job)
        }
        Log.d("area xml parsed", "Maps: ${maps.size}")

        jobs.forEach {
            val map = it.await()

            if (map != null) {
                maps.add(map)
            }


        }

        return SkiArea(id, name, liftCount, runCount, openingYear, website, regions, maps)

    }

    fun parseBase(areaId: Int): BaseSkiArea {
        val id: Int
        val name: String


        val url = URL("https://skimap.org/SkiAreas/view/$areaId.xml")
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(InputSource(url.openStream()))
        doc.documentElement.normalize()

        val nodeList = doc.getElementsByTagName("skiArea")


        val node = nodeList.item(0)
        val parent = node as Element
        id = parent.getAttribute("id").toInt()
        Log.d("area xml parsed", "ID: $id")

        val skiAreaName = parent.getElementsByTagName("name").item(0) as Element
        name = skiAreaName.textContent
        Log.d("area xml parsed", "Name: $name")

        return BaseSkiArea(id, name)


    }

}