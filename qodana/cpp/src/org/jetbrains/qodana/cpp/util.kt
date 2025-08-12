package org.jetbrains.qodana.cpp

import com.intellij.util.application
import org.jetbrains.qodana.staticAnalysis.inspections.config.qodanaAnalysisConfigForConfiguration

fun requestedCMakeProfileName(): String? {
  val config = application.qodanaAnalysisConfigForConfiguration
  checkNotNull(config) {
    "Qodana config was not loaded"
  }

  return config.cpp?.cmakePreset
}