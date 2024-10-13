package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.highlight.QodanaHighlightingListener

class CoverageHighlightingListener: QodanaHighlightingListener {
  override fun onStart(project: Project) {
    CoverageListenerService.getInstance(project)
  }
}