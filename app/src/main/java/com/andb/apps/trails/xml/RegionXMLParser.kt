package com.andb.apps.trails.xml

import android.database.sqlite.SQLiteConstraintException
import android.util.Log.d
import com.andb.apps.trails.database.regionsDao
import com.andb.apps.trails.objects.SkiRegion
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.lang.Exception
import java.net.URL
import javax.net.ssl.SSLException
import javax.xml.parsers.DocumentBuilderFactory

object RegionXMLParser {


    fun parseChildren(node: Element): List<Int> {
        if (node.getElementsByTagName("regions").length > 0) {
            val childNodes = (node.getElementsByTagName("regions").item(0) as Element).childNodes.toList()
            return childNodes.filter { it.isElement() }
                .map { (it as Element).getAttribute("id").toInt() }
        } else {
            return listOf()
        }
    }

    fun parseAreas(node: Element): List<Int> {
        if (node.getElementsByTagName("skiAreas").length > 0) {
            val childNodes = (node.getElementsByTagName("skiAreas").item(0) as Element).childNodes.toList()
            return childNodes.filter { it.isElement() }
                .map { (it as Element).getAttribute("id").toInt() }
        } else {
            return listOf()
        }

    }

    fun Node.isElement(): Boolean = nodeType == Node.ELEMENT_NODE

/*    fun downloadRegion(regionId: Int): SkiRegion? {
        try {
            d("downloadRegion", "downloading region id: $regionId")
            val region = getRegion(regionId)
            regionsDao().insertRegion(region)
            return region
        } catch (e: SSLException) {
            d("internetError", e.toString())
        } catch (e: SQLiteConstraintException) {
            d("duplicateError", e.toString())
        }
        return null
    }*/

    fun getRegion(regionId: Int): SkiRegion? {
        try {
            val node = getNode(regionId)
            return parse(node)
        } catch (e: Exception) {
            d("internetError", e.toString())
            return null
        }
    }


    private fun parse(node: Element): SkiRegion {
        d("base xml parseFull", "parsing")

        val id = node.getAttribute("id").toInt()
        d("region xml parsed", "ID: $id")

        val regionName = node.getElementsByTagName("name").item(0) as Element
        val name = regionName.textContent
        d("region xml parsed", "Name: $name")

        val regionMaps = node.getElementsByTagName("maps").item(0) as Element
        val mapCount = regionMaps.getAttribute("count").toInt()
        d("region xml parsed", "SkiMap Count: $mapCount")

        val parentRegions = node.getElementsByTagName("parents").item(0).childNodes.toList()
        val parentId = parentRegions
            .filter { it is Element }
            .map { (it as Element).getAttribute("level").toInt() }
            .sortedBy { it }
            .firstOrNull()

        val childRegions = ArrayList(parseChildren(node))
        val childAreas = ArrayList(parseAreas(node))

        return SkiRegion(id, name, mapCount, childRegions, childAreas, parentId)

    }

    private fun getNode(regionId: Int): Element {
        val url = URL("https://skimap.org/Regions/view/$regionId.xml")
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()

        val doc = db.parse(InputSource(url.openStream()))

        doc.documentElement.normalize()

        val nodeList = doc.getElementsByTagName("region")
        return nodeList.item(0) as Element
    }

}

fun NodeList.toList(): List<Node> {
    val children = ArrayList<Node>()
    for (c in 0 until length) {
        val item = item(c)
        children.add(item)
    }
    return children
}