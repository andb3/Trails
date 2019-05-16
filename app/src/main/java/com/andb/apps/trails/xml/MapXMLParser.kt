package com.andb.apps.trails.xml

import android.util.Log
import android.util.Log.d
import com.andb.apps.trails.database.mapsDao
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.objects.Thumbnail
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory


object MapXMLParser {


    fun downloadMap(mapId: Int): SkiMap? {
        val map = getMap(mapId)
        if (map != null) {
            mapsDao().insertMap(map)
        }
        return map
    }

    private fun getMap(mapId: Int): SkiMap? {
        try {
            val node = getNode(mapId)
            return parse(node)
        } catch (e: Exception) {
            d("internetError", e.toString())
            return null
        }

    }


    private fun getNode(mapId: Int): Element {
        //val url = URL("https://skimap.org/SkiMaps/view/$mapId.xml")
        //val raw = Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next()
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()
        val doc = db.parse("https://skimap.org/SkiMaps/view/$mapId.xml")
        //d("mapXml", raw)

        try {
            d("noImageAvailible", "mapId: $mapId, doc: ${doc == null}, documentElement: ${doc.documentElement == null}")
            doc.documentElement.normalize()
        } catch (e: Exception) {
            Log.d("noImageAvailable", "ID: $mapId")
            e.printStackTrace()
        }

        val nodeList = doc.getElementsByTagName("skiMap")
        return nodeList.item(0) as Element
    }

    private fun parse(parent: Element): SkiMap {

        val id = parent.getAttribute("id").toInt()
        val parentId = parseArea(parent)
        Log.d("map xml parsed", "Area: ${parentId}")
        val year = parseYear(parent)
        Log.d("map xml parsed", "Year: $year")
        val mainImage = parseImage(parent)

        val url = mainImage.first

        val thumbnails = parseThumbnails(parent, mainImage.second)

        return SkiMap(id, year, thumbnails, url, parentId)
    }

    private fun parseArea(element: Element): Int {
        val skiAreaTag = element.getElementsByTagName("skiArea").item(0) as Element
        return skiAreaTag.getAttribute("id").toInt()
    }

    private fun parseYear(element: Element): Int {
        val yearPublishedTag = element.getElementsByTagName("yearPublished").item(0) as Element
        return yearPublishedTag.textContent.toInt()
    }

    private fun parseThumbnails(element: Element, widthHeightRatio: Float): ArrayList<Thumbnail> {
        val imageTags = element.getElementsByTagName("thumbnail")
        val thumbnails = ArrayList<Thumbnail>()
        for (t in 0 until imageTags.length) {
            val skiMapThumbnail = imageTags.item(t) as Element

            val url = skiMapThumbnail.getAttribute("url")

            if (skiMapThumbnail.hasAttribute("width")) {
                val width = skiMapThumbnail.getAttribute("width").toInt()
                val height = width / widthHeightRatio
                thumbnails.add(Thumbnail(width, height.toInt(), url))
            } else {
                val height = skiMapThumbnail.getAttribute("height").toInt()
                val width = height * widthHeightRatio
                thumbnails.add(Thumbnail(width.toInt(), height, url))
            }

        }
        return thumbnails
    }


    private fun parseImage(element: Element): Pair<String, Float> {
        val imageTag: Element = element.getElementsByTagName("unprocessed").item(0) as Element
        val url = imageTag.getAttribute("url")
        if (imageTag.hasAttribute("width")) {
            val width = imageTag.getAttribute("width").toFloat()//TODO: pdfs have size not w+h
            val height = imageTag.getAttribute("height").toFloat()
            return Pair(url, width / height)
        } else {
            return Pair(url, (16 / 9).toFloat())
        }


    }

}


fun filenameFromURL(url: String): String {
    return url.drop("https://skimap.org/data/".length).replace('/', '.')
}

private fun isPdf(url: String): Boolean {
    return url.takeLast(4) == ".pdf"
}