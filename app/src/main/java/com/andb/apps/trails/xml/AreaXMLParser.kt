package com.andb.apps.trails.xml

import android.os.AsyncTask
import android.util.Log
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.database.regionAreaDao
import com.andb.apps.trails.objects.BaseSkiArea
import com.andb.apps.trails.objects.RegionAreaJoin
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiMap
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

object AreaXMLParser {

    fun getIndex(): NodeList {
        val url = URL("https://skimap.org/SkiAreas/index.xml")
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(InputSource(url.openStream()))
        doc.documentElement.normalize()

        return doc.getElementsByTagName("skiArea")
    }


    fun parseAll(nodeList: NodeList) {
        for (n in 0 until nodeList.length) {
            val node = nodeList.item(n) as Element
            val baseArea = parseBase(node)

            AsyncTask.execute {
                if (areasDao().getAreasById(baseArea.id).isEmpty()) {
                    areasDao().insertArea(baseArea)
                } else {
                    areasDao().updateArea(baseArea)
                }

                parseRegions(node).forEach { regionId ->
                    Log.d("creatingJoin", "regionId: $regionId, areaId: ${baseArea.id}")
                    val regionAreaJoin = RegionAreaJoin(regionId = regionId, areaId = baseArea.id)

                    if (!regionAreaDao().getJoinsByBoth(regionId, baseArea.id).contains(regionAreaJoin)) {
                        regionAreaDao().insert(regionAreaJoin)

                        Log.d("addingJoin", "Adding")
                    } else {
                        Log.d("updatingJoin", "Updating")
                        regionAreaDao().update(regionAreaJoin)
                    }

                }
            }
        }
    }

    suspend fun parseFull(areaId: Int, baseSkiArea: BaseSkiArea? = null): SkiArea {

        val maps = ArrayList<SkiMap>()

        val node = getNode(areaId)

        val skiArea = baseSkiArea ?: parseBase(node)

        //regions.addAll(parseRegions(node))
        //Log.d("area xml parsed", "Regions: ${regions.size}")

        val jobs = mutableListOf<Deferred<SkiMap?>>()

        val skiAreaMaps = node.getElementsByTagName("skiMap")
        for (m in 0 until skiAreaMaps.length) {
            val job = CoroutineScope(Dispatchers.IO).async {
                val mapTag = skiAreaMaps.item(m) as Element
                return@async MapXMLParser.parseThumbnail(mapTag.getAttribute("id").toInt())
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

        return SkiArea(skiArea, maps)

    }

    fun parseBase(areaId: Int): BaseSkiArea {
        return parseBase(getNode(areaId))
    }

    private fun parseBase(node: Node): BaseSkiArea {
        val id: Int
        val name: String


        val parent = node as Element
        id = parent.getAttribute("id").toInt()
        Log.d("area xml parsed", "ID: $id")

        val skiAreaName = parent.getElementsByTagName("name").item(0) as Element
        name = skiAreaName.textContent
        Log.d("area xml parsed", "Name: $name")

        val liftCount = parseLiftCount(node)
        Log.d("area xml parsed", "Lift Count: $liftCount")

        val runCount = parseRunCount(node)
        Log.d("area xml parsed", "Run Count: $runCount")

        val openingYear = parseOpeningYear(node)
        Log.d("area xml parsed", "Opening Year: $openingYear")

        val website = parseWebsite(node)
        Log.d("area xml parsed", "Website: $website")

        return BaseSkiArea(id, name, liftCount, runCount, openingYear, website)

    }

    private fun getNode(areaId: Int): Element {
        val url = URL("https://skimap.org/SkiAreas/view/$areaId.xml")
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(InputSource(url.openStream()))
        doc.documentElement.normalize()

        val nodeList = doc.getElementsByTagName("skiArea")
        return nodeList.item(0) as Element
    }

    private fun parseLiftCount(element: Element): Int {
        val skiAreaLiftCount = element.getElementsByTagName("liftCount").item(0) as Element?
        return skiAreaLiftCount?.textContent?.toInt() ?: -1
    }

    private fun parseRunCount(element: Element): Int {
        val skiAreaRunCount = element.getElementsByTagName("runCount").item(0) as Element?
        return skiAreaRunCount?.textContent?.toInt() ?: -1
    }

    private fun parseOpeningYear(element: Element): Int {
        val skiAreaOpeningYear = element.getElementsByTagName("openingYear").item(0) as Element?
        return skiAreaOpeningYear?.textContent?.toInt() ?: -1
    }

    private fun parseWebsite(element: Element): String {
        val skiAreaWebsite = element.getElementsByTagName("officialWebsite").item(0) as Element?
        return skiAreaWebsite?.textContent ?: ""
    }

    private fun parseRegions(element: Element): ArrayList<Int> {
        val regions = ArrayList<Int>()

        val skiAreaRegions = element.getElementsByTagName("region")
        for (r in 0 until skiAreaRegions.length) {
            val regionTag = skiAreaRegions.item(r) as Element
            val regionId = regionTag.getAttribute("id").toInt()
            if(regionId!=508/*no fantasy-land for now*/) {
                regions.add(regionId)
            }
        }
        return regions
    }
}