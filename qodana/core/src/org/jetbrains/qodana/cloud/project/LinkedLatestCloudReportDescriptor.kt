package org.jetbrains.qodana.cloud.project

import com.intellij.openapi.project.Project
import com.intellij.platform.util.progress.indeterminateStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.report.*

class LinkedLatestCloudReportDescriptor(
  project: Project,
  private val linked: LinkState.Linked,
  private val linkedCloudReportDescriptorBuilder: CloudReportDescriptorBuilder,
) : ReportDescriptor {
  override val isReportAvailableFlow: Flow<NotificationCallback?> =
    QodanaCloudProjectLinkService.getInstance(project).linkState.filterNot { it == linked }.map { null }

  override val browserViewProviderFlow: Flow<BrowserViewProvider> = emptyFlow()

  override val bannerContentProviderFlow: Flow<BannerContentProvider?> = emptyFlow()

  override val noProblemsContentProviderFlow: Flow<NoProblemsContentProvider> = emptyFlow()

  override suspend fun refreshReport(): ReportDescriptor? {
    return null
  }

  override suspend fun loadReport(project: Project): LoadedReport.Delegate? {
    val reportId = indeterminateStep(QodanaBundle.message("notification.title.cloud.report.loading.list")) {
      linkedCloudReportDescriptorBuilder.getLatestReportId()
    } ?: return null
    val delegate = LinkedCloudReportDescriptor(linked, reportId, project, isUploadedFromIde = false)
    return LoadedReport.Delegate(delegate)
  }

  override fun hashCode(): Int {
    return linked.hashCode()
  }

  override fun equals(other: Any?): Boolean {
    return this.linked == (other as? LinkedLatestCloudReportDescriptor)?.linked
  }
}