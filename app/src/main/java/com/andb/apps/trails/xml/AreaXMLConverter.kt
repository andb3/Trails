package com.andb.apps.trails.xml

import android.util.Log
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiAreaDetails
import com.andb.apps.trails.utils.toList
import okhttp3.ResponseBody
import org.w3c.dom.Element
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import javax.xml.parsers.DocumentBuilderFactory

class AreaXMLConverter : Converter<ResponseBody, SkiArea?> {
    override fun convert(value: ResponseBody): SkiArea? {
        Log.d("areaXMLConverter", "converting area")
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(value.byteStream())
        doc.documentElement.normalize()
        val node = doc.getElementsByTagName("skiArea").item(0) as Element
        return parse(node)
    }

    private fun parse(node: Element): SkiArea {


        val id: Int = node.getAttribute("id").toInt()

        //Log.d("area xml parsed", "ID: $id")

        val skiAreaName = node.getElementsByTagName("name").item(0) as Element
        val name = skiAreaName.textContent
        Log.d("area xml parsed", "Name: $name")

        val liftCount = parseLiftCount(node)
        //Log.d("area xml parsed", "Lift Count: $liftCount")

        val runCount = parseRunCount(node)
        //Log.d("area xml parsed", "Run Count: $runCount")

        val openingYear = parseOpeningYear(node)
        //Log.d("area xml parsed", "Opening Year: $openingYear")

        val website = parseWebsite(node)
        //Log.d("area xml parsed", "Website: $website")

        val maps = parseMaps(node)
        //Log.d("area xml parsed", "Map Count: ${maps.size}")

        val parentRegions = parseRegions(node)


        val details = SkiAreaDetails(liftCount, runCount, openingYear, website)

        return SkiArea(id, name, details, maps, parentRegions)

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
        //Log.d("area xml parsed", "map parse started, content: ${(element.getElementsByTagName("skiMaps").item(0) as Element).childNodes.length}")
        val skiAreaMaps = element.getElementsByTagName("skiMap")
        //Log.d("area xml parsed", "map parse element")
        val maps = skiAreaMaps.toList().map { (it as Element).getAttribute("id").toInt() }
        //Log.d("area xml parsed", "map return count ${maps.size}")


        return ArrayList(maps)
    }
}

class AreaXMLConverterFactory : Converter.Factory(){
    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
        return AreaXMLConverter()
    }
}