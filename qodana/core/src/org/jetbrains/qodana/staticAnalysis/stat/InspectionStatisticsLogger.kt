package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

class InspectionStatisticsLogger : QodanaWorkflowExtension {
  override val requireHeadless: Boolean = true

  override suspend fun beforeProjectClose(project: Project): Unit = coroutineScope {
    InspectionDurationsAggregatorService.getInstance(project).logDurations()
    InspectionFingerprintAggregatorService.getInstance(project).logFingerprint()

    val inspectionInfoService = project.serviceAsync<InspectionInfoQodanaReporterService>()
    launch { inspectionInfoService.logInspectionsSummaryInfo() }
    launch { inspectionInfoService.logDirectoriesInspectionsInfo() }
  }
}
