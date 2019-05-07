package com.andb.apps.trails.utils

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import com.github.rongi.klaster.KlasterBuilder
import jonathanfinerty.once.Amount
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main


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

fun dpToPx(dp: Int): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (dp * scale).toInt()
}

fun newIoThread(block: suspend CoroutineScope.()->Unit){
    CoroutineScope(Dispatchers.IO).launch(block = block)
}

suspend fun mainThread(block: suspend CoroutineScope.() -> Unit){
    withContext(Dispatchers.Main, block)
}

suspend fun ioThread(block: suspend CoroutineScope.() -> Unit){
    withContext(Dispatchers.IO, block)
}

fun KlasterBuilder.withHeader(){
    //TODO
}

fun KlasterBuilder.withFooter(){
    //TODO
}