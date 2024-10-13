package org.jetbrains.qodana.staticAnalysis

import com.intellij.openapi.application.EDT
import kotlinx.coroutines.Dispatchers
import org.jetbrains.annotations.BlockingExecutor
import kotlin.coroutines.CoroutineContext

val StaticAnalysisDispatchers: StaticAnalysisDispatchersProvider
  get() = StaticAnalysisDispatchersProvider

object StaticAnalysisDispatchersProvider {
  val Default: CoroutineContext
    get() = Dispatchers.Default

  val IO: @BlockingExecutor CoroutineContext
    get() = Dispatchers.IO

  val UI: CoroutineContext
    get() = Dispatchers.EDT
}