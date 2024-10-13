package org.jetbrains.qodana.report

import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.Result.BaselineState
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import java.nio.file.Path
import java.util.*

val SarifReport.guid: String
  get() = runs?.firstOrNull()?.automationDetails?.guid ?: UUID.randomUUID().toString()

val SarifReport.id: String?
  get() = runs?.firstOrNull()?.automationDetails?.id

val SarifReport.isQodanaReport: Boolean
  get() = runs.any { it.tool?.driver?.fullName?.contains("qodana", ignoreCase = true) ?: false }

val BaselineState?.isInBaseline: Boolean
  get() = this?.let { it != BaselineState.NEW } ?: false

fun addLocalReportAndHighlight(project: Project, reportDescriptor: LocalReportDescriptor) {
  QodanaLocalReportsService.getInstance(project).addReport(reportDescriptor)
  project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
    QodanaHighlightedReportService.getInstance(project).highlightReport(reportDescriptor)
  }
}

suspend fun openReportFromFileAndHighlight(project: Project, file: Path): FileReportDescriptor? {
  val reportDescriptor = project.let { FromFileReportDescriptorBuilder(file, it).createReportDescriptor() } ?: return null
  addLocalReportAndHighlight(project, reportDescriptor)
  return reportDescriptor
}