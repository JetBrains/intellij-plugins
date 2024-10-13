package org.jetbrains.qodana.cloud.project

import com.intellij.ide.BrowserUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.api.getErrorNotification
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.report.ReportDescriptorBuilder
import org.jetbrains.qodana.ui.QODANA_HELP_URL
import org.jetbrains.qodana.ui.problemsView.isLocalRunEnabled
import org.jetbrains.qodana.ui.run.RunQodanaAndPublishToCloudDialog
import java.nio.file.Path

class CloudReportDescriptorBuilder(
  private val linked: LinkState.Linked,
  private val project: Project,
) : ReportDescriptorBuilder<LinkedLatestCloudReportDescriptor> {
  private val projectDataProvider: QodanaCloudProjectDataProvider
    get() = linked.projectDataProvider

  private val cloudProjectId: String
    get() = projectDataProvider.projectPrimaryData.id

  override suspend fun createReportDescriptor(): LinkedLatestCloudReportDescriptor {
    return LinkedLatestCloudReportDescriptor(project, linked, this)
  }

  suspend fun createReportDescriptorWithId(reportId: String): LinkedCloudReportDescriptor {
    projectDataProvider.setLatestNoNotificationReportId(reportId)
    return LinkedCloudReportDescriptor(linked, reportId, project)
  }

  suspend fun createPublishedReportDescriptor(sarifPath: Path): LinkedCloudReportDescriptor? {
    val reportId = getLatestReportId() ?: return null
    QodanaReportDownloader.getInstance(project).addPublishedReport(sarifPath, cloudProjectId, reportId)
    return LinkedCloudReportDescriptor(linked, reportId, project)
  }

  suspend fun getLatestReportId(): String? {
    return when(val reportIdResponse = projectDataProvider.refreshLatestReportIdWithoutNotification()) {
      is QDCloudResponse.Success -> {
        val reportId = reportIdResponse.value
        if (reportId == null) {
          getNoAnyRunsFoundNotification().notify(project)
        }
        return reportId
      }
      is QDCloudResponse.Error -> {
        thisLogger().warn("Failed loading Qodana Cloud report", reportIdResponse.exception)
        reportIdResponse.getErrorNotification(QodanaBundle.message("notification.title.cloud.report.failed.load")).notify(project)
        return null
      }
      null -> {
        null
      }
    }
  }

  private fun getNoAnyRunsFoundNotification(): Notification {
    val notification = QodanaNotifications.General.notification(
      QodanaBundle.message("notification.title.cloud.report.failed.load"),
      LinkedCloudReportDescriptor.FailMessagesProvider.getNoAnyRunsFoundMessage(),
      NotificationType.INFORMATION,
      withQodanaIcon = true
    )
    if (isLocalRunEnabled()) {
      notification.addAction(
        NotificationAction.create(QodanaBundle.message("qodana.run.action.only.cloud")) { _ ->
          RunQodanaAndPublishToCloudDialog(project, linked).show()
        }
      )
    }
    notification.addAction(
      NotificationAction.createSimple(QodanaBundle.message("qodana.get.report.help.button")) {
        BrowserUtil.browse(QODANA_HELP_URL)
      }
    )

    return notification
  }
}
