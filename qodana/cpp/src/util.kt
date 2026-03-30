package org.jetbrains.qodana.cpp

import com.intellij.util.application
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.qodanaAnalysisConfigForConfiguration

internal val qodanaConfig: QodanaConfig
  get() = application.qodanaAnalysisConfigForConfiguration ?: error("Qodana config was not loaded")