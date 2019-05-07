package com.andb.apps.trails.xml

import android.util.Log
import android.util.Log.d
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiAreaDetails
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringWriter
import java.net.URL
import javax.net.ssl.SSLException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


object AreaXMLParser {

    fun getIndex(): NodeList {
        val url = URL("https://skimap.org/SkiAreas/index.xml")
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(InputSource(url.openStream()))
        doc.documentElement.normalize()

        return doc.getElementsByTagName("skiArea")
    }

/*    fun downloadArea(areaId: Int): SkiArea? {
        try {
            val area = getArea(areaId)
            areasDao().insertArea(area)
            return area
        } catch (e: SSLException) {
            Log.d("internetError", e.toString())
            return null
        }
    }*/

    fun getArea(areaId: Int): SkiArea?{
        try {
            val node = getNode(areaId)
            return parse(node)
        }catch (e: Exception){
            d("internetError", e.toString())
            return null
        }

    }

    private fun parse(node: Element): SkiArea {


        val id: Int = node.getAttribute("id").toInt()

        Log.d("area xml parsed", "ID: $id")

        val skiAreaName = node.getElementsByTagName("name").item(0) as Element
        val name = skiAreaName.textContent
        Log.d("area xml parsed", "Name: $name")

        val liftCount = parseLiftCount(node)
        Log.d("area xml parsed", "Lift Count: $liftCount")

        val runCount = parseRunCount(node)
        Log.d("area xml parsed", "Run Count: $runCount")

        val openingYear = parseOpeningYear(node)
        Log.d("area xml parsed", "Opening Year: $openingYear")

        val website = parseWebsite(node)
        Log.d("area xml parsed", "Website: $website")

        val maps = parseMaps(node)
        Log.d("area xml parsed", "Map Count: ${maps.size}")

        val parentRegions = parseRegions(node)


        val details = SkiAreaDetails(liftCount, runCount, openingYear, website)

        return SkiArea(id, name, details, maps, parentRegions)

    }

    private fun getNode(areaId: Int): Element {
        val url = URL("https://skimap.org/SkiAreas/view/$areaId.xml")
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(InputSource(url.openStream()))
        try {
            doc.documentElement.normalize()
        } catch (e: Exception) {
            Log.e("areaParseFail", "failed for area $areaId")
            //e.printStackTrace()
            throw e
        }

        Log.d("getNode", doc.toXMLString())

        val nodeList = doc.getElementsByTagName("skiArea")
        return nodeList.item(0) as Element
    }

    private fun Document.toXMLString(): String {
        val tf = TransformerFactory.newInstance()
        val transformer = tf.newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        val writer = StringWriter()
        transformer.transform(DOMSource(this), StreamResult(writer))
        return writer.buffer.toString().replace("\n|\r", "")
    }

    private fun parseLiftCount(element: Element): Int? {
        val skiAreaLiftCount = element.getElementsByTagName("liftCount").item(0) as Element?
        return skiAreaLiftCount?.textContent?.toInt()
    }

    private fun parseRunCount(element: Element): Int? {
        val skiAreaRunCount = element.getElementsByTagName("runCount").item(0) as Element?
        return skiAreaRunCount?.textContent?.toInt()
    }

    private fun parseOpeningYear(element: Element): Int? {
        val skiAreaOpeningYear = element.getElementsByTagName("openingYear").item(0) as Element?
        return skiAreaOpeningYear?.textContent?.toInt()
    }

    private fun parseWebsite(element: Element): String? {
        val skiAreaWebsite = element.getElementsByTagName("officialWebsite").item(0) as Element?
        return skiAreaWebsite?.textContent
    }

    private fun parseRegions(element: Element): ArrayList<Int> {
        val regions = ArrayList<Int>()

        val skiAreaRegions = element.getElementsByTagName("region")
        for (r in 0 until skiAreaRegions.length) {
            val regionTag = skiAreaRegions.item(r) as Element
            val regionId = regionTag.getAttribute("id").toInt()
            if (regionId != 508/*no fantasy-land for now*/) {
                regions.add(regionId)
            }
        }
        return regions
    }

    private fun parseMaps(element: Element): ArrayList<Int> {
        Log.d("area xml parsed", "map parse started, content: ${(element.getElementsByTagName("skiMaps").item(0) as Element).childNodes.length}")
        val skiAreaMaps = element.getElementsByTagName("skiMap")
        Log.d("area xml parsed", "map parse element")
        val maps = skiAreaMaps.toList().map { (it as Element).getAttribute("id").toInt() }
        Log.d("area xml parsed", "map return count ${maps.size}")


        return ArrayList(maps)
    }
}