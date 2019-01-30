package com.andb.apps.trails.xml

import android.graphics.drawable.Drawable
import android.util.Log
import com.andb.apps.trails.objects.BaseSkiArea
import com.andb.apps.trails.objects.Map
import com.andb.apps.trails.objects.MapArtist
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory


object MapXMLParser {
    fun parse(mapId: Int, loadThumbnail: Boolean = false): Map? {

        val id: Int
        val skiArea: BaseSkiArea
        val image: Drawable
        val year: Int
        val artist: MapArtist




        val url = URL("https://skimap.org/SkiMaps/view/$mapId.xml")
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(InputSource(url.openStream()))

        try {
            doc.documentElement.normalize()
        }catch (e: Exception){
            Log.d("noImageAvailible", "ID: $mapId")
            e.printStackTrace()
            return null
        }



        val nodeList = doc.getElementsByTagName("skiMap")


        val node = nodeList.item(0)
        val parent = node as Element
        id = parent.getAttribute("id").toInt()



        val skiAreaTag = parent.getElementsByTagName("skiArea").item(0) as Element
        skiArea = BaseSkiArea(skiAreaTag.getAttribute("id").toInt(), skiAreaTag.textContent)

        Log.d("map xml parsed", "Area: ${skiArea.name}")

        val yearPublishedTag = parent.getElementsByTagName("yearPublished").item(0) as Element
        year = yearPublishedTag.textContent.toInt()

        Log.d("map xml parsed", "Year: $year")


        val artistTag = parent.getElementsByTagName("artist").item(0)
        artist = if (artistTag as Element? != null) {
            artistTag as Element
            MapArtist(artistTag.getAttribute("id").toInt(), artistTag.textContent)

        } else {
            MapArtist(-1, "")
        }


        Log.d("map xml parsed", "Artist: ${artist.name}")

        var imageUrl = ""

        if (loadThumbnail) {
            val imageTags = parent.getElementsByTagName("thumbnail")
            var imageWidth = 1000
            for (t in 0 until imageTags.length) {
                val skiMapThumbnail = imageTags.item(t) as Element
                if (skiMapThumbnail.hasAttribute("width")) {
                    val width = skiMapThumbnail.getAttribute("width").toInt()
                    if (width < imageWidth) {
                        val tempUrl = skiMapThumbnail.getAttribute("url")
                        if (!tempUrl.isEmpty()) {
                            imageWidth = width
                            imageUrl = tempUrl
                        }
                    }
                }
            }
        } else {

            val imageTag: Element
            val renderedImage = parent.getElementsByTagName("render").item(0)
            val unprocessedImage = parent.getElementsByTagName("unprocessed").item(0)
            imageTag = if (renderedImage as Element? != null) {
                renderedImage as Element
            } else {
                unprocessedImage as Element
            }
            imageUrl = imageTag.getAttribute("url")

        }



        Log.d("map xml parsed", "Image URL: $imageUrl")


        val urlContent = URL(imageUrl).content as InputStream
        image = Drawable.createFromStream(urlContent, null)

        val map = Map(id, skiArea, image, year, artist)

        Log.d("map xml parsing success", map.toString())

        return map

    }
}