package com.andb.apps.trails.download

import com.andb.apps.trails.database.Database
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.xml.AreaXMLParser
import java.lang.Exception

fun updateAreas():Boolean {
    return try {
        val nodeList = AreaXMLParser.getIndex()
        if (nodeList.length != areasDao().getSize()) {
            AreaXMLParser.parseAll(nodeList)
        }
        true
    }catch (e: Exception){
        false
    }
}