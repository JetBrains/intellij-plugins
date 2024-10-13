package org.jetbrains.qodana.protocol

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.QodanaIntelliJYamlService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.getErrorNotification
import org.jetbrains.qodana.cloud.project.*
import org.jetbrains.qodana.cloud.userApi
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.highlight.HighlightedReportState
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.report.QodanaLocalReportsService
import org.jetbrains.qodana.report.ReportDescriptor
import org.jetbrains.qodana.stats.*

suspend fun highlightOpenInIdeOneMarker(project: Project, openInIdeProblemParameters: OpenInIdeProblemParameters) {
  val reportDescriptor = SingleMarkerReportDescriptorBuilder(project, openInIdeProblemParameters).createReportDescriptor()
  QodanaLocalReportsService.getInstance(project).addReport(reportDescriptor)

  highlightOpenInIdeReportDescriptor(project, reportDescriptor, openInIdeProblemParameters, activateCoverage = false)
}

suspend fun highlightOpenInIdeCloudReport(
  project: Project,
  authorized: UserState.Authorized,
  reportId: String,
  activateCoverage: Boolean,
  openInIdeProblemParameters: OpenInIdeProblemParameters?
) {
  val projectIdAndNameResponse = qodanaCloudResponse {
    val api = authorized.userApi().value()

    val projectId = api.getReportData(reportId).value().projectId
    val projectName = api.getProjectProperties(projectId).value().name
    projectId to projectName
  }
  val (projectId, projectName) = when(projectIdAndNameResponse) {
    is QDCloudResponse.Success -> {
      projectIdAndNameResponse.value
    }
    is QDCloudResponse.Error -> {
      projectIdAndNameResponse.getErrorNotification(QodanaBundle.message("notification.title.cloud.project.failed.load")).notify(project)
      return
    }
  }

  val linkState = QodanaCloudProjectLinkService.getInstance(project).linkState.value
  val (reportDescriptor, notification) = when {
    linkState is LinkState.Linked && linkState.projectDataProvider.projectPrimaryData.id == projectId -> {
      val linkedDeferred = CompletableDeferred(linkState)
      val reportDescriptor = OpenInIdeCloudReportDescriptor(linkedDeferred, authorized, reportId, projectId, projectName, project)
      reportDescriptor to null
    }
    else -> {
      val linkedDeferred = CompletableDeferred<LinkState.Linked>()
      val reportDescriptor = OpenInIdeCloudReportDescriptor(linkedDeferred, authorized, reportId, projectId, projectName, project)
      reportDescriptor to linkToCloudNotification(linkedDeferred, project, authorized, linkState, projectId, projectName)
    }
  }

  val success = highlightOpenInIdeReportDescriptor(project, reportDescriptor, openInIdeProblemParameters, activateCoverage)
  if (success) {
    notification?.notify(project)
  }
}

private suspend fun highlightOpenInIdeReportDescriptor(
  project: Project,
  reportDescriptor: ReportDescriptor,
  openInIdeProblemParameters: OpenInIdeProblemParameters?,
  activateCoverage: Boolean,
): Boolean {
  val highlightedReportService = QodanaHighlightedReportService.getInstance(project)

  val selected = highlightedReportService.highlightReport(reportDescriptor, activateCoverage = activateCoverage)
  logStats(reportDescriptor)
  if (openInIdeProblemParameters == null) return false

  if (selected !is HighlightedReportState.Selected ||
      selected.highlightedReportData.sourceReportDescriptor != reportDescriptor) return false

  val matchingSarifProblem = selected.highlightedReportData.allProblems.firstOrNull { openInIdeProblemParameters.matchesSarifProblem(it) }
                             ?: return true
  selected.highlightedReportData.requestNavigateToProblem(matchingSarifProblem)
  return true
}

private fun logStats(reportDescriptor: ReportDescriptor) {
  QodanaPluginStatsCounterCollector.OPEN_IN_IDE.log(
    OpenInIdeProtocol.SHOW_MARKER,
    OpenInIdeResult.SUCCESS
  )
  QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
    true,
    reportDescriptor.toStatsReportType(),
    SourceHighlight.OPEN_IN_IDE
  )
}

private fun OpenInIdeProblemParameters.matchesSarifProblem(problem: SarifProblem): Boolean {
  return this.path == problem.relativePathToFile &&
         this.column - 1 == problem.startColumn &&
         this.line - 1 == problem.startLine &&
         this.markerLength == problem.charLength &&
         this.revisionId.isNullOrEqualsTo(problem.revisionId) &&
         this.inspectionId.isNullOrEqualsTo(problem.inspectionId) &&
         this.severity.isNullOrEqualsTo(problem.qodanaSeverity)
}

private fun Any?.isNullOrEqualsTo(other: Any?): Boolean {
  return this?.let { this == other } ?: true
}

private fun linkToCloudNotification(
  linkedDeferred: CompletableDeferred<LinkState.Linked>,
  project: Project,
  authorized: UserState.Authorized,
  linkState: LinkState,
  projectId: String,
  @NlsSafe projectName: String?,
): Notification? {
  if (QodanaIntelliJYamlService.getInstance(project).disableOpenInIdeLinkNotification) return null

  val notification = QodanaNotifications.General.notification(
    QodanaBundle.message("notification.link.project.to.cloud.title", project.name),
    QodanaBundle.message("notification.link.project.to.cloud.text", projectName ?: "qodana.cloud"),
    NotificationType.INFORMATION,
    withQodanaIcon = true
  )
  notification.addAction(NotificationAction.createSimpleExpiring(QodanaBundle.message("notification.link.project.to.cloud.action.text")) {
    project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
      val organizationIdResponse = qodanaCloudResponse {
        authorized.userApi().value()
          .getProjectProperties(projectId).value().organizationId
      }
      val organizationId = when(organizationIdResponse) {
        is QDCloudResponse.Success -> {
          organizationIdResponse.value
        }
        is QDCloudResponse.Error -> {
          organizationIdResponse.getErrorNotification(QodanaBundle.message("notification.title.cloud.project.failed.load")).notify(project)
          return@launch
        }
      }
      val notLinked = when(linkState) {
        is LinkState.NotLinked -> linkState
        is LinkState.Linked -> linkState.unlink() ?: return@launch
      }
      val linked = notLinked.linkWithQodanaCloudProject(
        authorized,
        CloudProjectData(
          CloudProjectPrimaryData(projectId, CloudOrganizationPrimaryData((organizationId))),
          CloudProjectProperties(projectName)
        )
      )
      if (linked != null) {
        linkedDeferred.complete(linked)
      }
    }
  })
  return notification
}