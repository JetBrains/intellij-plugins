package org.jetbrains.qodana.staticAnalysis.inspections.metrics.inspections.cyclomaticComplexity

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile

interface CyclomaticComplexityMetricFileVisitor {
  companion object {
    val EP = ExtensionPointName<CyclomaticComplexityMetricFileVisitor>("org.intellij.qodana.cyclomaticComplexityFileVisitor")
  }

  val language: String

  fun visit(file: PsiFile): List<CyclomaticComplexityMethodData>
}