package org.jetbrains.qodana.extensions

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.openapi.extensions.ExtensionPointName
import org.jetbrains.qodana.highlight.QodanaHighlightingInfoType

interface QodanaHighlightInfoComparator {
  companion object {
    private val EP_NAME: ExtensionPointName<QodanaHighlightInfoComparator> =
      ExtensionPointName.create("org.intellij.qodana.qodanaHighlightInfoComparator")

    fun equals(otherHighlight: HighlightInfo, qodanaHighlight: HighlightInfo) : Boolean {
      val highlightType = qodanaHighlight.type
      require(highlightType is QodanaHighlightingInfoType) { "$qodanaHighlight is not Qodana-related" }
      for (e in EP_NAME.extensionList) {
        return e.equals(otherHighlight, qodanaHighlight) ?: continue
      }
      val type = qodanaHighlight.type as QodanaHighlightingInfoType
      return (otherHighlight.inspectionToolId == type.sarifProblem.inspectionId || otherHighlight.externalSourceId == type.sarifProblem.inspectionId) &&
             otherHighlight.actualStartOffset == qodanaHighlight.actualStartOffset &&
             otherHighlight.actualEndOffset == qodanaHighlight.actualEndOffset
    }
  }

  fun equals(otherHighlight: HighlightInfo, qodanaHighlight: HighlightInfo) : Boolean?
}