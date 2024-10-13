package org.jetbrains.qodana.staticAnalysis.inspections.metrics.problemDescriptors

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ex.ProblemDescriptorImpl
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.MetricFileData

class MetricCodeDescriptor(
  val fileData: MetricFileData,
  val element: PsiElement,
  rangeInElement: TextRange? = null,
  descriptionTemplate: @InspectionMessage String = QodanaBundle.message("code.metrics.file.description"),
  highlightType: ProblemHighlightType = ProblemHighlightType.WARNING,
  onTheFly: Boolean = true,
  vararg fixes: LocalQuickFix,
) : ProblemDescriptorImpl(element, element, descriptionTemplate, fixes, highlightType, false, rangeInElement, onTheFly)