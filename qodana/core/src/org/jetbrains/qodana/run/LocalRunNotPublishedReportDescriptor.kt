package org.jetbrains.qodana.run

import com.intellij.openapi.project.Project
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.report.*
import org.jetbrains.qodana.webUi.QodanaWebUiService

class LocalRunNotPublishedReportDescriptor(
  val fileReportDescriptor: FileReportDescriptor,
  private val notificationIfFileNotPresent: Boolean = true
) : LocalReportDescriptor by fileReportDescriptor {
  override suspend fun loadReport(project: Project): LoadedReport.Sarif? {
    return fileReportDescriptor
      .loadReportAndSpawnNotificationIfNeeded(fileReportDescriptor.project, notificationIfFileNotPresent)
      ?.let { LoadedReport.Sarif(it.validatedSarif, it.aggregatedReportMetadata, QodanaBundle.message("qodana.report.local.analysis")) }
  }

  override val browserViewProviderFlow: Flow<BrowserViewProvider>
    get() = createBrowserViewProviderFlow()

  override val bannerContentProviderFlow: Flow<BannerContentProvider?>
    get() = createBannerContentProviderFlow()

  override suspend fun refreshReport(): ReportDescriptor = this

  private fun createBrowserViewProviderFlow(): Flow<BrowserViewProvider> {
    val qodanaInIdeRunsResults = QodanaRunInIdeService.getInstance(fileReportDescriptor.project).runsResults
    return qodanaInIdeRunsResults
      .map { outputs ->
        val qodanaOutputWithMatchingGuid = outputs.associateBy { it.reportGuid }[fileReportDescriptor.reportGuid]
        val qodanaConverterInput = qodanaOutputWithMatchingGuid?.let { QodanaConverterInput.FullQodanaOutput(it.path) }
                                   ?: QodanaConverterInput.SarifFileOnly(fileReportDescriptor.reportPath)

        BrowserViewProviderImpl(qodanaConverterInput)
      }
  }

  override fun hashCode(): Int = fileReportDescriptor.hashCode()

  override fun equals(other: Any?): Boolean {
    return this.fileReportDescriptor == (other as? LocalRunNotPublishedReportDescriptor)?.fileReportDescriptor
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun createBannerContentProviderFlow(): Flow<BannerContentProvider?> {
    val qodanaInIdeRunsResults = QodanaRunInIdeService.getInstance(fileReportDescriptor.project).runsResults

    val browserViewProviderFlow = qodanaInIdeRunsResults.flatMapLatest { inIdeRunResults ->
      val reportIsPresentInIdeRuns = inIdeRunResults.any { it.reportGuid == this.fileReportDescriptor.reportGuid }
      if (reportIsPresentInIdeRuns) browserViewProviderFlow else flowOf(null)
    }
    return BannerContentProvider.openBrowserAndSetupCIBannerFlow(
      fileReportDescriptor.project,
      QodanaBundle.message("problems.toolwindow.banner.run.local.text"),
      QodanaBundle.message("problems.toolwindow.banner.run.local.run.openWebUi.text"),
      browserViewProviderFlow
    )
  }

  private inner class BrowserViewProviderImpl(val qodanaConverterInput: QodanaConverterInput) : BrowserViewProvider {
    override suspend fun openBrowserView() {
      QodanaWebUiService.getInstance(fileReportDescriptor.project).requestOpenBrowserWebUi(fileReportDescriptor.reportGuid, qodanaConverterInput)
    }
  }
}