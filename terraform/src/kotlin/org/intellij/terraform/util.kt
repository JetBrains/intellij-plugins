// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform

import com.intellij.openapi.util.text.StringUtil
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicReference

fun String.nullize(nullizeSpaces: Boolean = false): String? {
  return StringUtil.nullize(this, nullizeSpaces)
}

fun <T> Iterator<T>.firstOrNull(): T? {
  if (!hasNext())
    return null
  return next()
}

fun joinCommaOr(list: List<String>): String = when (list.size) {
  0 -> ""
  1 -> list.first()
  else -> (list.dropLast(1).joinToString(postfix = " or " + list.last()))
}

fun <T> executeLatest(f: suspend () -> T): suspend () -> T {
  val deferredRef = AtomicReference<Deferred<T>>()
  return {
    coroutineScope {
      while (coroutineContext.isActive) {
        val newDeferred = async(start = CoroutineStart.LAZY) { f() }
        val prev = deferredRef.get()
        prev?.cancel()
        if (deferredRef.compareAndSet(prev, newDeferred)) {
          try {
            return@coroutineScope newDeferred.await()
          }
          catch (e: CancellationException) {
            coroutineContext.ensureActive()
          }
          finally {
            deferredRef.compareAndSet(newDeferred, null)
          }
        }
        newDeferred.cancel()
        while (coroutineContext.isActive) {
          val storedDeferred = deferredRef.get() ?: break
          try {
            return@coroutineScope storedDeferred.await()
          }
          catch (e: CancellationException) {
            coroutineContext.ensureActive()
          }
        }
      }
      coroutineContext.ensureActive()
      throw IllegalStateException("Unexpected state during `executeLatest` call")
    }
  }
}
