package org.jetbrains.qodana.run

import kotlinx.coroutines.Deferred
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlConfig
import java.nio.file.Path

data class RunInIdeParameters(
  val qodanaYamlConfig: QodanaYamlConfig,
  val qodanaYamlConfigText: String?,
  val qodanaYamlFile: Path?,
  val qodanaBaseline: Path?,
)

sealed interface QodanaRunState {
  interface Running : QodanaRunState {
    val outputFuture: Deferred<QodanaInIdeOutput?>

    fun cancel()
  }

  interface NotRunning : QodanaRunState {
    fun run(runInIdeParameters: RunInIdeParameters): Running?
  }
}