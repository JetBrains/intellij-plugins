package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.platform.backend.observation.ActivityKey

object QodanaActivityKey : ActivityKey {
  override val presentableName: String = "qodana"
}