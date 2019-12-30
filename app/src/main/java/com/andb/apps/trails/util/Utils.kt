package com.andb.apps.trails.util

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.*


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


fun dpToPx(dp: Int): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (dp * scale).toInt()
}

fun pxToDp(px: Int):Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (px/scale).toInt()
}

val Int.dp
    get() = dpToPx(this)

fun newIoThread(block: suspend CoroutineScope.() -> Unit): Job {
    return CoroutineScope(Dispatchers.IO).launch(block = block)
}

suspend fun mainThread(block: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.Main, block)
}

suspend fun ioThread(block: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.IO, block)
}

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

    open fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem

}

fun time(tag: String = "timedOperation", block: () -> Unit) {
    val start = System.nanoTime().toFloat()
    block.invoke()
    val end = System.nanoTime().toFloat()
    Log.d(tag, "time: ${(end - start) / 1000000} ms")
}

fun filenameFromURL(url: String): String{
    return url.drop("https://skimap.org/data/".length).replace('/', '.')
}

fun Fragment.putArguments(block: Bundle.() -> Unit) {
    val bundle = this.arguments ?: Bundle()
    block.invoke(bundle)
    this.arguments = bundle
}

infix fun <T> Collection<T>.equalsUnordered(other: Collection<T>): Boolean {
    // check collections aren't same
    if (this !== other) {
        // fast check of sizes
        if (this.size != other.size) return false
        val areNotEqual = this.asSequence()
            // check other contains next element from this
            .map { it in other }
            // searching for first negative answer
            .contains(false)
        if (areNotEqual) return false
    }
    // collections are same or they are contains same elements
    return true
}

infix fun Int.toUnordered(to: Int): IntProgression {
    return if (this > to) to..this else this..to
}