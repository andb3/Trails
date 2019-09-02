package com.andb.apps.trails.xml

import android.util.Log
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.objects.Thumbnail
import okhttp3.ResponseBody
import org.w3c.dom.Element
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.NullPointerException
import java.lang.reflect.Type
import javax.xml.parsers.DocumentBuilderFactory

class MapXMLConverter : Converter<ResponseBody, SkiMap?> {
    override fun convert(value: ResponseBody): SkiMap? {
        Log.d("mapXMLConverter", "converting map")
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(value.byteStream())
        try{
            doc.documentElement.normalize()
        }catch (e: NullPointerException){
            e.printStackTrace()
            Log.e("normalizeError", "doc: $doc, documentElement: ${doc.documentElement}")
            return null
        }
        val node = doc.getElementsByTagName("skiMap").item(0) as Element
        return parse(node)
    }

    private fun parse(parent: Element): SkiMap {

        val id = parent.getAttribute("id").toInt()
        val parentId = parseArea(parent)
        Log.d("map xml parsed", "Area: $parentId")
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

class MapXMLConverterFactory : Converter.Factory(){
    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
        return MapXMLConverter()
    }
}

fun filenameFromURL(url: String): String{
    return url.drop("https://skimap.org/data/".length).replace('/', '.')
}

private fun isPdf(url: String): Boolean{
    return url.takeLast(4) == ".pdf"
}