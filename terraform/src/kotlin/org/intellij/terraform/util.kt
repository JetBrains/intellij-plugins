// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform

import com.intellij.openapi.util.text.StringUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.util.concurrent.atomic.AtomicReference

fun String.nullize(nullizeSpaces:Boolean = false): String? {
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

class ExecuteLatest<T>(private val scope: CoroutineScope, private val f: suspend () -> T) : suspend () -> T {

  private val deferredRef = AtomicReference<Deferred<T>>()

  override suspend fun invoke(): T {
    return restart().await()
  }

  tailrec fun restart(): Deferred<T> {
    val newDeferred = scope.async(start = CoroutineStart.LAZY) { f.invoke() }
    val prev = deferredRef.get()
    prev?.cancel()
    if (deferredRef.compareAndSet(prev, newDeferred)) {
      newDeferred.invokeOnCompletion {
        deferredRef.compareAndSet(newDeferred, null)
      }
      newDeferred.start()
      return newDeferred
    }
    else {
      return deferredRef.get() ?: restart()
    }
  }

}
