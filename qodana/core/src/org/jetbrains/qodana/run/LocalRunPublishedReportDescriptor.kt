package org.jetbrains.qodana.run

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.report.*

class LocalRunPublishedReportDescriptor(
  val fileReportDescriptor: FileReportDescriptor,
  val publishedReportLink: String,
  private val notificationIfFileNotPresent: Boolean = true
) : LocalReportDescriptor by fileReportDescriptor {
  override val browserViewProviderFlow: Flow<BrowserViewProvider> = flowOf(BrowserViewProviderImpl(publishedReportLink))

  override val bannerContentProviderFlow: Flow<BannerContentProvider?>
    get() = createBannerContentProviderFlow()

  override suspend fun refreshReport(): ReportDescriptor = this

  override suspend fun loadReport(project: Project): LoadedReport.Sarif? {
    return fileReportDescriptor
      .loadReportAndSpawnNotificationIfNeeded(fileReportDescriptor.project, notificationIfFileNotPresent)
      ?.let { LoadedReport.Sarif(it.validatedSarif, it.aggregatedReportMetadata, QodanaBundle.message("qodana.report.local.analysis")) }
  }

  private fun createBannerContentProviderFlow(): Flow<BannerContentProvider?> {
    return BannerContentProvider.openBrowserAndSetupCIBannerFlow(
      fileReportDescriptor.project,
      QodanaBundle.message("problems.toolwindow.banner.run.cloud.text"),
      QodanaBundle.message("problems.toolwindow.banner.run.cloud.openWebUi.text"),
      browserViewProviderFlow
    )
  }

  override fun hashCode(): Int = fileReportDescriptor.hashCode()

  override fun equals(other: Any?): Boolean {
    if (other !is LocalRunPublishedReportDescriptor) return false
    return this.fileReportDescriptor == other.fileReportDescriptor
  }

  private class BrowserViewProviderImpl(private val link: String) : BrowserViewProvider {
    override suspend fun openBrowserView() {
      BrowserUtil.open(link)
    }
  }
}