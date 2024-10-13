package org.jetbrains.qodana.highlight

import com.intellij.codeInsight.highlighting.TooltipLinkHandler
import com.intellij.openapi.editor.Editor

class SarifInspectionDescriptionLinkHandler : TooltipLinkHandler() {
  companion object {
    private const val LINK_REFERENCE = "#sarifInspection/"

    fun getLinkReferenceToInspection(inspectionId: String): String {
      return LINK_REFERENCE + inspectionId
    }
  }
  override fun getDescription(refSuffix: String, editor: Editor): String? {
    val project = editor.project ?: return null
    val highlightedReportData = QodanaHighlightedReportService.getInstance(project).highlightedReportState.value.highlightedReportDataIfSelected
                                ?: return null
    return highlightedReportData.inspectionsInfoProvider.getDescription(inspectionId = refSuffix)
  }
}
