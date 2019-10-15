package com.andb.apps.trails.utils

import android.content.res.Resources
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.rongi.klaster.Klaster
import com.github.rongi.klaster.KlasterBuilder
import kotlinx.coroutines.*
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.text.Normalizer


fun TextView.showIfAvailable(value: Int?, stringID: Int) {

    if (!value.isNullOrNegative()) {
        text = String.format(context.getString(stringID), value)
        visibility = View.VISIBLE

    } else {
        visibility = View.GONE
    }

}

fun TextView.showIfAvailable(value: String?, stringID: Int) {

    if (!value.isNullOrEmpty()) {
        text = String.format(context.getString(stringID), value)
        visibility = View.VISIBLE

    } else {
        visibility = View.GONE
    }

}

private fun Int?.isNullOrNegative() = this == null || this < 0

fun Int.isNegative(includeZero: Boolean = false) = this < if (includeZero) 0 else 1
fun Int.isPositive(includeZero: Boolean = false) = this > if (includeZero) 0 else -1


fun <T> MutableCollection<T>.dropBy(amount: Int = 1) {
    for (i in 0 until amount) {
        val element = this.last()
        remove(element)
    }
}

fun <T> Collection<T>.applyEach(block: T.() -> Unit) {
    for (i in this){
        i.apply(block)
    }
}

fun <T> Collection<T>.equalsUnordered(other: Collection<T>): Boolean {
    return this.containsAll(other) && other.containsAll(this)
}

/*
fun <T> MutableLiveData<T>.refresh() {
    this.value = this.value
}
*/

fun dpToPx(dp: Int): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (dp * scale).toInt()
}

fun newIoThread(block: suspend CoroutineScope.() -> Unit): Job {
    return CoroutineScope(Dispatchers.IO).launch(block = block)
}

suspend fun mainThread(block: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.Main, block)
}

suspend fun ioThread(block: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.IO, block)
}

fun KlasterBuilder.withHeader(): KlasterBuilder {
    //TODO

    return this
}

fun KlasterBuilder.withFooter(): KlasterBuilder {
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

@Suppress("REIFIED_TYPE_PARAMETER_NO_INLINE")
fun <reified T : Any> Any?.isListOf(): Boolean {
    return this is List<*> && this.isListOf<T>()
}

@Suppress("REIFIED_TYPE_PARAMETER_NO_INLINE")
fun <reified T : Any> List<*>.isListOf(): Boolean =
    T::class.java.isAssignableFrom(this::class.java.componentType)

infix fun <T> T.and(other: T) = listOf(this, other)
infix fun <T> List<T>.and(other: T) = this.toMutableList().also { it.add(other) }


abstract class DiffCallback<T>(private val newList: List<T>, private val oldList: List<T>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])

    abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])

    open fun areContentsTheSame(oldItem: T, newItem: T): Boolean = areItemsTheSame(oldItem, newItem)

}

fun <T> MutableList<T>.addNotNull(element: T?) {
    if (element != null) {
        this.add(element)
    }
}

private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun CharSequence?.unaccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "")
}

fun <T> Collection<T>.intersects(other: Collection<T>) = any(other::contains)

fun time(tag: String = "timedOperation", block: () -> Unit) {
    val start = System.nanoTime().toFloat()
    block.invoke()
    val end = System.nanoTime().toFloat()
    Log.d(tag, "time: ${(end - start) / 1000000} ms")
}

fun filenameFromURL(url: String): String{
    return url.drop("https://skimap.org/data/".length).replace('/', '.')
}

class InitialLiveData<T>(private val initialValue: T): MediatorLiveData<T>(){
    override fun getValue(): T {
        return super.getValue() ?: initialValue
    }
}