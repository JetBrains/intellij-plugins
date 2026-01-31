// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import java.util.concurrent.atomic.AtomicReference

/**
 * A utility class that executes a suspend function [body] and cancels any previous invocation if it was still running.
 * This way, only the "latest" invocation will complete its execution.
 *
 * Semantically close to the [kotlinx.coroutines.flow.collectLatest] but doesn't need a Flow instance and has different guarantees.
 *
 * Example in usage:
 *
 * ```kotlin
 * val executor = LatestInvocationRunner {
 *     delay(1000) // debounce
 *     heavyLongRunningTask() // will be called only after 1000ms pause after the last `EVENT_THAT_HAPPENS_MANY_TIMES_IN_A_ROW` happens
 * }
 *
 * subscribe(EVENT_THAT_HAPPENS_MANY_TIMES_IN_A_ROW) {
 *     launch {
 *         executor.cancelPreviousAndRun() // Cancels if there's a still running task and runs a new invocation
 *     }
 * }
 * ```
 *
 * @see kotlinx.coroutines.flow.collectLatest
 */
class LatestInvocationRunner<T>(private val body: suspend () -> T) {

  private val currentComputation = AtomicReference<Deferred<T>>()

  /**
   * Cancels any previous execution of [body] (if it was still running) and runs the [body] again.
   *
   * Note: Only the execution of [body] is canceled, the call of [cancelPreviousAndRun] continues to wait
   * until the latest call of [body] is completed, and returns its value.
   * Thus, this method ensures that [body] execution completes before returning.
   *
   * @return The result of the latest invocation of [body].
   */
  suspend fun cancelPreviousAndRun(): T {
   return coroutineScope {
     while (coroutineContext.isActive) {
       val newDeferred = async(start = CoroutineStart.LAZY) { body() }
       val prev = currentComputation.get()
       prev?.cancel()
       if (currentComputation.compareAndSet(prev, newDeferred)) {
         try {
           return@coroutineScope newDeferred.await()
         }
         catch (e: CancellationException) {
           coroutineContext.ensureActive()
         }
         finally {
           currentComputation.compareAndSet(newDeferred, null)
         }
       }
       newDeferred.cancel()
       while (coroutineContext.isActive) {
         val storedDeferred = currentComputation.get() ?: break
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