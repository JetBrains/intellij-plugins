package org.jetbrains.qodana.staticAnalysis.inspections.runner.log

import com.intellij.openapi.extensions.ExtensionPointName
import org.jetbrains.qodana.staticAnalysis.inspections.runner.TimeCookie

interface QodanaLoggingActivity {
  companion object {
    val EP_NAME: ExtensionPointName<QodanaLoggingActivity> = ExtensionPointName<QodanaLoggingActivity>("org.intellij.qodana.loggingActivity")
  }

  suspend fun executeActivity(progressName: String, timeCookie: TimeCookie)
}