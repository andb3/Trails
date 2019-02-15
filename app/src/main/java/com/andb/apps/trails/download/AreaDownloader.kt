package com.andb.apps.trails.download

import com.andb.apps.trails.InitialDownloadService
import com.andb.apps.trails.database.Database
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.xml.AreaXMLParser
import org.w3c.dom.NodeList
import java.lang.Exception
import java.lang.NullPointerException

suspend fun updateAreas(service: InitialDownloadService, nodeList: NodeList? = null):Boolean {
    return try {
        val nodes = nodeList ?: AreaXMLParser.getIndex()
        if (nodes.length != areasDao().getSize()) {
            AreaXMLParser.parseAll(service, nodes)
        }
        true
    /*}catch (e){*/
    }catch (e: Exception){
        false
    }
}