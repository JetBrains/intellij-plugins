package org.jetbrains.qodana.report

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.NotificationContent
import com.jetbrains.qodana.sarif.model.SarifReport
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.notifications.QodanaNotifications

private const val QODANA_ABOUT_SARIF_FORMAT_URL = "https://www.jetbrains.com/help/qodana/qodana-sarif-output.html"

object ReportValidator {
  fun validateReport(report: SarifReport): ReportResult<ValidatedSarif, ValidatorError> {
    return getValidatorError(report)?.let { ReportResult.Fail(it) } ?: ReportResult.Success(ValidatedSarif(report))
  }

  private fun getValidatorError(report: SarifReport): ValidatorError? {
    return when {
      report.runs == null -> NoRuns
      report.runs.isEmpty() -> EmptyRuns
      report.runs.all { it.results == null } -> NoResults
      report.runs.all { it.tool == null } -> NoTool
      else -> null
    }
  }

  fun getNotificationContentFromSource(validationError: ValidatorError, reportSource: String): @NotificationContent String {
    return when(validationError) {
      NoRuns -> QodanaBundle.message("notification.content.failed.validation.no.runs", reportSource)
      EmptyRuns -> QodanaBundle.message("notification.content.failed.validation.empty.runs", reportSource)
      NoResults -> QodanaBundle.message("notification.content.failed.validation.no.results", reportSource)
      NoTool -> QodanaBundle.message("notification.content.failed.validation.no.tool", reportSource)
    }
  }

  sealed class ValidatorError : ReportResult.Error<ValidatorError> {
    override fun throwException(): Nothing = error("Error during report validation: $this")

    override fun spawnNotification(project: Project?, contentProvider: (ValidatorError) -> @NotificationContent String) {
      val notification = QodanaNotifications.General.notification(
        QodanaBundle.message("notification.title.cant.parse.report"),
        contentProvider.invoke(this),
        NotificationType.WARNING
      )
      notification.addAction(
        NotificationAction.createSimple(QodanaBundle.message("notification.action.about.qodana.sarif.format"))
        { BrowserUtil.browse(QODANA_ABOUT_SARIF_FORMAT_URL) }
      )
      notification.notify(project)
    }
  }

  object NoRuns : ValidatorError()

  object EmptyRuns : ValidatorError()

  object NoResults : ValidatorError()

  object NoTool : ValidatorError()
}