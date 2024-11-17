package org.jetbrains.qodana

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun ExtensionPointName<*>.epUpdatedFlow(): Flow<Unit> {
  return callbackFlow {
    val disposable = Disposer.newDisposable()
    this@epUpdatedFlow.addChangeListener(
      { trySendBlocking(Unit) },
      disposable
    )
    awaitClose { Disposer.dispose(disposable) }
  }
}