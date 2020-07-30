package com.andb.apps.trails.util

import kotlinx.coroutines.*

fun newIoThread(block: suspend CoroutineScope.() -> Unit): Job {
    return CoroutineScope(Dispatchers.IO).launch(block = block)
}

suspend fun mainThread(block: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.Main, block)
}

suspend fun ioThread(block: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.IO, block)
}

fun <T> asyncIO(block: suspend CoroutineScope.() -> T): Deferred<T> {
    return CoroutineScope(Dispatchers.IO).async(block = block)
}
