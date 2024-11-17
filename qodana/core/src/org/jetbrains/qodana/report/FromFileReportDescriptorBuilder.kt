package org.jetbrains.qodana.report

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import java.nio.file.Path

class FromFileReportDescriptorBuilder(path: Path, private val project: Project) : ReportDescriptorBuilder<FileReportDescriptor> {
  private val reportPath: Path = path.toAbsolutePath()

  override suspend fun createReportDescriptor(): FileReportDescriptor? {
    return runInterruptible(QodanaDispatchers.IO) {
      val report = when(val readReportResult = ReportReader.readReport(reportPath)) {
        is ReportResult.Fail -> {
          readReportResult.error.spawnNotification(project) {
            getReportFileErrorNotificationContent(reportPath, it)
          }
          return@runInterruptible null
        }
        is ReportResult.Success -> readReportResult.loadedSarifReport.sarif
      }
      val guid = report.guid
      val name = report.id ?: guid

      FileReportDescriptor(reportPath, report.isQodanaReport, guid, name, project)
    }
  }
}

fun getReportFileErrorNotificationContent(
  reportPath: Path,
  readerError: ReportReader.ReaderError
): @NlsContexts.NotificationContent String {
  return when(readerError) {
    is ReportReader.FailedValidation -> {
      ReportValidator.getNotificationContentFromSource(readerError.validatorError, QodanaBundle.message("notification.content.failed.validation.report.from.file", reportPath))
    }
    is ReportReader.FailedParsing -> {
      when(readerError.parserError) {
        ReportParser.FileNotExists -> QodanaBundle.message("notification.content.report.file.does.not.exist", reportPath)
        is ReportParser.JsonParseFailed -> QodanaBundle.message("notification.content.cant.parse.report.from.file", reportPath,
                                                                readerError.parserError.message)
      }
    }
  }
}