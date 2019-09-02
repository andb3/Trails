package com.andb.apps.trails.xml

import android.util.Log
import com.andb.apps.trails.database.Database.Companion.db
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.utils.isElement
import com.andb.apps.trails.utils.toList
import okhttp3.ResponseBody
import org.w3c.dom.Element
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.reflect.Type
import javax.xml.parsers.DocumentBuilderFactory

class RegionXMLConverter : Converter<ResponseBody, SkiRegion?> {
    override fun convert(value: ResponseBody): SkiRegion? {
        Log.d("regionXMLConverter", "converting region")
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(value.byteStream())
        doc.documentElement.normalize()
        val node = doc.getElementsByTagName("region").item(0) as Element
        return parse(node)
    }

    private fun parse(node: Element): SkiRegion {

        val id = node.getAttribute("id").toInt()

        val regionName = node.getElementsByTagName("name").item(0) as Element
        val name = regionName.textContent

        val regionMaps = node.getElementsByTagName("maps").item(0) as Element
        val mapCount = regionMaps.getAttribute("count").toInt()

        val parentRegions = node.getElementsByTagName("parents").item(0).childNodes.toList()
        val parentId = parentRegions
            .filterIsInstance<Element>()
            .map { it.getAttribute("level").toInt() }
            .minBy { it }

        val childRegions = ArrayList(parseChildren(node))
        val childAreas = ArrayList(parseAreas(node))

        return SkiRegion(id, name, mapCount, childRegions, childAreas, parentId)

    }

    private fun parseAreas(node: Element): List<Int> {
        if (node.getElementsByTagName("skiAreas").length > 0) {
            val childNodes = (node.getElementsByTagName("skiAreas").item(0) as Element).childNodes.toList()
            return childNodes.filter { it.isElement() }
                .map { (it as Element).getAttribute("id").toInt() }
        } else {
            return listOf()
        }

    }

    private fun parseChildren(node: Element): List<Int> {
        if (node.getElementsByTagName("regions").length > 0) {
            val childNodes = (node.getElementsByTagName("regions").item(0) as Element).childNodes.toList()
            return childNodes.filter { it.isElement() }
                .map { (it as Element).getAttribute("id").toInt() }
        } else {
            return listOf()
        }
    }
}

class RegionXMLConverterFactory : Converter.Factory(){
    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
        return RegionXMLConverter()
    }
}