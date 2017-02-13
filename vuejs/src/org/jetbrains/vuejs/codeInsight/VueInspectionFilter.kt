package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.lang.javascript.highlighting.IntentionAndInspectionFilter
import com.sixrr.inspectjs.validity.BadExpressionStatementJSInspection

class VueInspectionFilter : IntentionAndInspectionFilter() {
  override fun isSupportedInspection(inspectionToolId: String?): Boolean =
    inspectionToolId != InspectionProfileEntry.getShortName(BadExpressionStatementJSInspection::class.java.simpleName)
}