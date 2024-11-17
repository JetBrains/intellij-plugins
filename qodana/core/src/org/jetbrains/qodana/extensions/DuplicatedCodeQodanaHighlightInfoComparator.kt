package org.jetbrains.qodana.extensions

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import org.jetbrains.qodana.highlight.QodanaHighlightingInfoType

class DuplicatedCodeQodanaHighlightInfoComparator : QodanaHighlightInfoComparator {
  private val inspectionName = "DuplicatedCode"

  override fun equals(otherHighlight: HighlightInfo, qodanaHighlight: HighlightInfo): Boolean? {
    val type = qodanaHighlight.type as QodanaHighlightingInfoType
    if (type.sarifProblem.inspectionId != inspectionName) return null
    if (otherHighlight.inspectionToolId != inspectionName && otherHighlight.externalSourceId != inspectionName) return null
    return otherHighlight.actualStartOffset == qodanaHighlight.actualStartOffset
  }
}