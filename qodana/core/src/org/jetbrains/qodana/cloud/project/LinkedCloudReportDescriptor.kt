package org.jetbrains.qodana.cloud.project

import com.google.common.base.Objects
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.report.*
import org.jetbrains.qodana.ui.link.LinkCloudProjectDialog
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel

class LinkedCloudReportDescriptor(
  private val linked: LinkState.Linked,
  val reportId: String,
  private val project: Project,
  private val isUploadedFromIde: Boolean = false,
  private val doDownload: Boolean = true,
) : ReportDescriptor {
  val projectId: String
    get() = linked.projectDataProvider.projectPrimaryData.id

  override val isReportAvailableFlow: Flow<NotificationCallback?> =
    QodanaCloudProjectLinkService.getInstance(project).linkState.filterNot { it == linked }.map { null }

  private val browserViewProvider = BrowserViewProvider.qodanaCloudReport(projectId, reportId)

  override val browserViewProviderFlow: Flow<BrowserViewProvider> = flowOf(browserViewProvider)

  override val bannerContentProviderFlow: Flow<BannerContentProvider?> = createBannerContentProviderFlow()

  override val noProblemsContentProviderFlow: Flow<NoProblemsContentProvider> = flowOf(NoProblemsContentProviderImpl())

  override suspend fun refreshReport(): ReportDescriptor? {
    val newReportId = withBackgroundProgress(project, QodanaBundle.message("notification.title.cloud.report.loading.list")) {
      linked.cloudReportDescriptorBuilder.getLatestReportId()
    } ?: return null
    return LinkedCloudReportDescriptor(linked, newReportId, project, isUploadedFromIde)
  }

  override suspend fun loadReport(project: Project): LoadedReport.Sarif? {
    return QodanaReportDownloader.getInstance(project).getReport(linked.authorized, reportId, projectId, doDownload)
  }

  private fun createBannerContentProviderFlow(): Flow<BannerContentProvider?> {
    if (!isUploadedFromIde) return emptyFlow()

    return BannerContentProvider.openBrowserAndSetupCIBannerFlow(
      project,
      QodanaBundle.message("problems.toolwindow.banner.run.cloud.text"),
      QodanaBundle.message("problems.toolwindow.banner.run.cloud.openWebUi.text"),
      browserViewProviderFlow
    )
  }

  override fun hashCode(): Int = Objects.hashCode(reportId, projectId)

  override fun equals(other: Any?): Boolean {
    if (other !is LinkedCloudReportDescriptor) return false
    return reportId == other.reportId && projectId == other.projectId
  }

  object FailMessagesProvider {
    fun getQodanaCloudOfflineMessage() = QodanaBundle.message("qodana.cloud.offline")

    fun getCloudReportNotFoundMessage() = QodanaBundle.message("qodana.cloud.no.report.found")

    fun getNoAnyRunsFoundMessage() = QodanaBundle.message("qodana.cloud.no.runs.found")
  }

  private inner class NoProblemsContentProviderImpl : NoProblemsContentProvider {
    @Suppress("DialogTitleCapitalization")
    override fun noProblems(qodanaProblemsViewModel: QodanaProblemsViewModel): NoProblemsContentProvider.NoProblemsContent {
      val openQodanaCloudAction = NoProblemsContentProvider.ActionDescriptor(
        QodanaBundle.message("no.problems.content.no.problems.cloud.report.action")
      ) { _, _ ->
        browserViewProvider.openBrowserView()
      }
      return NoProblemsContentProvider.NoProblemsContent(
        title = QodanaBundle.message("no.problems.content.no.problems.title.no.problems.found"),
        description = QodanaBundle.message("no.problems.content.no.problems.cloud.report.description"),
        actions = openQodanaCloudAction to null
      )
    }

    override fun notMatchingProject(
      qodanaProblemsViewModel: QodanaProblemsViewModel,
      totalProblemsCount: Int
    ): NoProblemsContentProvider.NoProblemsContent {
      val cloudProjectName = linked.projectDataProvider.projectProperties.value.lastLoadedValue?.asSuccess()?.name
                             ?: linked.projectDataProvider.projectPrimaryData.id

      val linkOtherProjectAction = NoProblemsContentProvider.ActionDescriptor(
        QodanaBundle.message("no.problems.content.not.matched.linked.report.action")
      ) { _, _ ->
        withContext(QodanaDispatchers.Ui) {
          LinkCloudProjectDialog(project).show()
        }
      }

      return NoProblemsContentProvider.NoProblemsContent(
        title = QodanaBundle.message("no.problems.content.not.matched.cloud.report.title"),
        description = QodanaBundle.message("no.problems.content.not.matched.linked.report.description", cloudProjectName, totalProblemsCount, project.name),
        actions = linkOtherProjectAction to null
      )
    }
  }
}