package com.andb.apps.trails.xml

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import com.andb.apps.trails.download.FileDownloader
import com.andb.apps.trails.objects.BaseSkiArea
import com.andb.apps.trails.objects.BaseSkiMap
import com.andb.apps.trails.objects.MapArtist
import com.andb.apps.trails.objects.SkiMap
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory


object MapXMLParser {
    fun parseFull(
        mapId: Int,
        save: Boolean = false
    ): SkiMap? {
        val parent = getNode(mapId) ?: return null
        val baseMap = parseBase(parent) ?: return null

        val imageUrl = parseImage(parent)
        Log.d("map xml parsed", "Image URL: $imageUrl")


        if (isPdf(imageUrl)) {
            preloadPDF(imageUrl)
        }

        val map = SkiMap(baseMap, imageUrl)
        Log.d("map xml parsing success", map.toString())
        return map
    }

    fun parseThumbnail(mapId: Int, size: Int = 300): SkiMap?{
        val parent = getNode(mapId) ?: return null

        val startTime = System.currentTimeMillis()
        val baseMap = parseBase(parent) ?: return null

        val imageUrl = parseThumbnails(parent, size)

        val map = SkiMap(baseMap, imageUrl)
        Log.d("map xml parsing success", map.toString() + "time: ${(System.currentTimeMillis() - startTime)} ms")

        return map
    }

    private fun getNode(mapId: Int): Element?{
        val url = URL("https://skimap.org/SkiMaps/view/$mapId.xml")
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(InputSource(url.openStream()))

        try {
            doc.documentElement.normalize()
        } catch (e: Exception) {
            Log.d("noImageAvailible", "ID: $mapId")
            e.printStackTrace()
            return null
        }

        val nodeList = doc.getElementsByTagName("skiMap")
        return nodeList.item(0) as Element
    }

    private fun parseBase(parent: Element): BaseSkiMap?{

        val id = parent.getAttribute("id").toInt()
        val area = parseArea(parent)
        Log.d("map xml parsed", "Area: ${area.name}")
        val year = parseYear(parent)
        Log.d("map xml parsed", "Year: $year")

        return BaseSkiMap(id, area, year)
    }

    private fun parseArea(element: Element):BaseSkiArea{
        val skiAreaTag = element.getElementsByTagName("skiArea").item(0) as Element
        return BaseSkiArea(skiAreaTag.getAttribute("id").toInt(), skiAreaTag.textContent)
    }

    private fun parseYear(element: Element): Int{
        val yearPublishedTag = element.getElementsByTagName("yearPublished").item(0) as Element
        return yearPublishedTag.textContent.toInt()
    }

    private fun parseArtist(element: Element): MapArtist{
        val artistTag = element.getElementsByTagName("artist").item(0)
        return if (artistTag as Element? != null) {
            artistTag as Element
            MapArtist(artistTag.getAttribute("id").toInt(), artistTag.textContent)

        } else {
            MapArtist(-1, "")
        }
    }

    private fun parseThumbnails(element: Element, size: Int = 100): String{
        val imageTags = element.getElementsByTagName("thumbnail")
        var imageWidth = 1000
        var imageUrl = ""
        for (t in 0 until imageTags.length) {
            val skiMapThumbnail = imageTags.item(t) as Element
            if (skiMapThumbnail.hasAttribute("width")) {
                val width = skiMapThumbnail.getAttribute("width").toInt()
                if (width in size..(imageWidth - 1)) {
                    val tempUrl = skiMapThumbnail.getAttribute("url")
                    if (!tempUrl.isEmpty()) {
                        imageWidth = width
                        imageUrl = tempUrl
                    }
                }
            }
        }
        return imageUrl
    }


    private fun parseImage(element: Element): String{
        val imageTag: Element = element.getElementsByTagName("unprocessed").item(0) as Element
        return imageTag.getAttribute("url")
    }


    private fun preloadPDF(url: String){
        val filename = filenameFromURL(url)
        CoroutineScope(Dispatchers.IO).launch {
            CoroutineScope(Dispatchers.IO).launch {
                FileDownloader.downloadFile(url, filename)
            }
        }
    }


    fun filenameFromURL(url: String): String{
        return url.drop("https://skimap.org/data/".length).replace('/', '.')
    }

    fun isPdf(url: String): Boolean{
        return url.takeLast(4) == ".pdf"
    }
}