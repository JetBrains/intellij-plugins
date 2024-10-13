package org.jetbrains.qodana.report

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.run.QodanaConverterInput
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel
import org.jetbrains.qodana.webUi.QodanaWebUiService
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.isRegularFile

private val REPORT_AVAILABILITY_REFRESH_PERIOD = Duration.ofSeconds(1)

class FileReportDescriptor(
  val reportPath: Path,
  val isQodanaReport: Boolean,
  val reportGuid: String,
  val reportName: String,
  val project: Project,
) : LocalReportDescriptor {
  private val reportUnavailableDeferred = CompletableDeferred<UnavailableType>()

  override val isReportAvailableFlow: Flow<NotificationCallback?> = merge(
    flow {
      while (true) {
        delay(REPORT_AVAILABILITY_REFRESH_PERIOD.toMillis())
        if (!reportPath.isRegularFile()) {
          emit(UnavailableType.MISSING_FILE)
          break
        }
      }
    }.flowOn(QodanaDispatchers.Default),
    flow {
      emit(reportUnavailableDeferred.await())
    }
  ).map { unavailableType ->
    val notificationCallback = { notifyNotAvailable(unavailableType) }
    notificationCallback
  }

  private val browserViewProvider: BrowserViewProvider? = createBrowserViewProvider()

  override val browserViewProviderFlow: Flow<BrowserViewProvider> = browserViewProvider?.let { flowOf(it) } ?: emptyFlow()

  override val bannerContentProviderFlow: Flow<BannerContentProvider?> = emptyFlow()

  override val noProblemsContentProviderFlow: Flow<NoProblemsContentProvider> = flowOf(NoProblemsContentProviderImpl())

  override suspend fun refreshReport(): ReportDescriptor = this

  override fun checkAvailability(): Boolean {
    val isAvailable = reportPath.isRegularFile()
    if (!isAvailable) {
      reportUnavailableDeferred.complete(UnavailableType.MISSING_FILE)
    }
    return !reportUnavailableDeferred.isCompleted
  }

  override fun markAsUnavailable() {
    reportUnavailableDeferred.complete(UnavailableType.MARKED_AS_UNAVAILABLE)
  }

  private fun notifyNotAvailable(unavailableType: UnavailableType) {
    if (unavailableType == UnavailableType.MARKED_AS_UNAVAILABLE) return
    val readReportResult = ReportReader.FailedParsing(ReportParser.FileNotExists)
    readReportResult.spawnNotification(project, ::getNotificationContent)
  }

  private fun createBrowserViewProvider(): BrowserViewProvider? {
    return if (isQodanaReport) BrowserViewProviderImpl(QodanaConverterInput.SarifFileOnly(reportPath)) else null
  }

  override fun getName(highlightedState: LocalReportDescriptor.HighlightedState): String = reportName

  override suspend fun loadReport(project: Project): LoadedReport.Sarif? {
    return loadReportAndSpawnNotificationIfNeeded(project, notificationIfFileNotPresent = true)
  }

  suspend fun loadReportAndSpawnNotificationIfNeeded(project: Project, notificationIfFileNotPresent: Boolean): LoadedReport.Sarif? {
    return runInterruptible(QodanaDispatchers.IO) {
      when(val readReportResult = ReportReader.readReport(reportPath)) {
        is ReportResult.Fail -> {
          val fileNotExists = (readReportResult.error as? ReportReader.FailedParsing)?.parserError is ReportParser.FileNotExists
          if (fileNotExists && !notificationIfFileNotPresent) {
            return@runInterruptible null
          }
          readReportResult.error.spawnNotification(project, ::getNotificationContent)
          null
        }
        is ReportResult.Success -> {
          LoadedReport.Sarif(readReportResult.loadedSarifReport, AggregatedReportMetadata(emptyMap()), reportPath.toString())
        }
      }
    }
  }

  override fun hashCode(): Int = reportGuid.hashCode()

  override fun equals(other: Any?): Boolean {
    if (other !is FileReportDescriptor) return false

    return reportGuid == other.reportGuid
  }

  fun getNotificationContent(readerError: ReportReader.ReaderError): @NlsContexts.NotificationContent String {
    return when(readerError) {
      is ReportReader.FailedValidation -> {
        ReportValidator.getNotificationContentFromSource(readerError.validatorError, QodanaBundle.message("notification.content.failed.validation.report.from.file.with.id", reportPath, reportName))
      }
      is ReportReader.FailedParsing -> {
        when(readerError.parserError) {
          ReportParser.FileNotExists -> QodanaBundle.message("notification.content.report.file.does.not.exist.w.id", reportPath, reportName)
          is ReportParser.JsonParseFailed -> QodanaBundle.message("notification.content.cant.parse.report.from.file.w.id", reportName,
                                                                  reportPath, readerError.parserError.message)
        }
      }
    }
  }

  private inner class BrowserViewProviderImpl(val qodanaConverterInput: QodanaConverterInput) : BrowserViewProvider {
    override suspend fun openBrowserView() {
      QodanaWebUiService.getInstance(project).requestOpenBrowserWebUi(reportGuid, qodanaConverterInput)
    }
  }

  private inner class NoProblemsContentProviderImpl : NoProblemsContentProvider {
    override fun noProblems(qodanaProblemsViewModel: QodanaProblemsViewModel): NoProblemsContentProvider.NoProblemsContent {
      val title = QodanaBundle.message("no.problems.content.no.problems.title.no.problems.found")
      return if (browserViewProvider != null) {
        val openLocalUiAction = NoProblemsContentProvider.ActionDescriptor(
          QodanaBundle.message("no.problems.content.no.problems.local.report.w.browser.action")
        ) { _, _ ->
          browserViewProvider.openBrowserView()
        }
        NoProblemsContentProvider.NoProblemsContent(
          title = title,
          description = QodanaBundle.message("no.problems.content.no.problems.local.report.w.browser.description"),
          actions = openLocalUiAction to null
        )
      } else {
        NoProblemsContentProvider.NoProblemsContent(
          title = title,
          description = QodanaBundle.message("no.problems.content.no.problems.local.report.no.browser.description"),
          actions = null
        )
      }
    }

    override fun notMatchingProject(
      qodanaProblemsViewModel: QodanaProblemsViewModel,
      totalProblemsCount: Int
    ): NoProblemsContentProvider.NoProblemsContent {
      return NoProblemsContentProvider.NoProblemsContent(
        title = QodanaBundle.message("no.problems.content.not.matched.local.report.title"),
        description = QodanaBundle.message("no.problems.content.not.matched.local.report.description", totalProblemsCount, project.name),
        actions = NoProblemsContentProvider.openOtherReportAction() to NoProblemsContentProvider.openOtherProjectAction()
      )
    }
  }


  private enum class UnavailableType {
    MISSING_FILE,
    MARKED_AS_UNAVAILABLE
  }
}
