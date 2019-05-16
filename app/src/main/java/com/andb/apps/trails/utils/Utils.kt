package com.andb.apps.trails.utils

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.rongi.klaster.KlasterBuilder
import jonathanfinerty.once.Amount
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.net.ConnectivityManager
import androidx.recyclerview.widget.DiffUtil
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList


fun TextView.showIfAvailable(value: Int?, stringId: Int) {

    if (!value.isNullOrNegative()) {
        text = String.format(context.getString(stringId), value)
        visibility = View.VISIBLE

    } else {
        visibility = View.GONE
    }

}

fun TextView.showIfAvailable(value: String?, stringId: Int) {

    if (!value.isNullOrEmpty()) {
        text = String.format(context.getString(stringId), value)
        visibility = View.VISIBLE

    } else {
        visibility = View.GONE
    }

}

private fun Int?.isNullOrNegative() = this == null || this < 0

fun Int.isNegative(includeZero: Boolean = false) = this < if(includeZero) 0 else 1
fun Int.isPositive(includeZero: Boolean = false) = this > if(includeZero) 0 else -1


fun <T> MutableCollection<T>.dropBy(amount: Int = 1){
    for(i in 0 until amount){
        val element = this.last()
        remove(element)
    }
}

fun <T> Collection<T>.applyEach(block: T.()->Unit){
    this.forEach(block)
}

fun <T> Collection<T>.equalsUnordered(other: Collection<T>): Boolean{
    return this.containsAll(other) && other.containsAll(this)
}

fun <T> MutableLiveData<T>.refresh(){
    this.value = this.value
}

fun dpToPx(dp: Int): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (dp * scale).toInt()
}

fun newIoThread(block: suspend CoroutineScope.()->Unit): Job{
    return CoroutineScope(Dispatchers.IO).launch(block = block)
}

suspend fun mainThread(block: suspend CoroutineScope.() -> Unit){
    withContext(Dispatchers.Main, block)
}

suspend fun ioThread(block: suspend CoroutineScope.() -> Unit){
    withContext(Dispatchers.IO, block)
}

fun KlasterBuilder.withHeader(): KlasterBuilder{
    //TODO

    return this
}

fun KlasterBuilder.withFooter(): KlasterBuilder{
    //TODO
    return this
}

fun NodeList.toList(): List<Node> {
    val children = ArrayList<Node>()
    for (c in 0 until length) {
        val item = item(c)
        children.add(item)
    }
    return children
}
fun Node.isElement(): Boolean = nodeType == Node.ELEMENT_NODE



infix fun <T> T.and(other: T) = listOf(this, other)

private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

abstract class DiffCallback<T>(private val newList: List<T>, private val oldList: List<T>) : DiffUtil.Callback(){

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    open fun areContentsTheSame(oldItem: T, newItem: T): Boolean = areItemsTheSame(oldItem, newItem)

}