package org.jetbrains.qodana.report

import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.Flow

/**
 * General abstraction for working with SARIF report, contains primary info about the report and also loads the report itself.
 * See [ReportDescriptorBuilder]
 */
interface ReportDescriptor {
  /**
   * Determine if report is available (i.e. [loadReport] probably will not return `null`)
   * Not available reports are being filtered out.
   * If report becomes unavailable, [isReportAvailableFlow] emits callback, which could be used to notify user
   * that report is no longer available. After emitting report becomes unhighlighted
   */
  val isReportAvailableFlow: Flow<NotificationCallback?>

  val browserViewProviderFlow: Flow<BrowserViewProvider>

  val bannerContentProviderFlow: Flow<BannerContentProvider?>

  val noProblemsContentProviderFlow: Flow<NoProblemsContentProvider>

  /**
   * Invoked "Refresh Report" action in tab, may return current instance or for cloud report new report with refreshed report id
   * in case of null nothing will happen, in case of not null – report will be forcefully reloaded
   */
  suspend fun refreshReport(): ReportDescriptor?

  suspend fun loadReport(project: Project): LoadedReport?

  override fun hashCode(): Int

  override fun equals(other: Any?): Boolean
}

sealed interface LoadedReport {
  /**
   * Load another report descriptor and delegate loading to it
   */
  class Delegate(val reportDescriptorDelegate: ReportDescriptor) : LoadedReport

  /**
   * Chain of [Delegate]s should always end with [Sarif]
   */
  class Sarif(
    val validatedSarif: ValidatedSarif,
    val aggregatedReportMetadata: AggregatedReportMetadata,
    val reportName: String,
  ): LoadedReport
}

typealias NotificationCallback = () -> Unit