package com.andb.apps.trails.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.broadcastIn
import kotlinx.coroutines.flow.zip

fun <T1, T2, T3, R> Flow<T1>.zip(
    second: Flow<T2>,
    third: Flow<T3>,
    transform: suspend (T1, T2, T3) -> R
): Flow<R> = this
    .zip(second) { a, b -> a to b }
    .zip(third) { (a, b), c -> transform(a, b, c) }


fun <T> Flow<T>.broadcast(scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): BroadcastChannel<T> {
    return broadcastIn(scope)
}