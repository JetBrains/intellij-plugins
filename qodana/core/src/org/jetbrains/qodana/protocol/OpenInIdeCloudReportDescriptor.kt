package org.jetbrains.qodana.protocol

import com.google.common.base.Objects
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.project.LinkState
import org.jetbrains.qodana.cloud.project.LinkedCloudReportDescriptor
import org.jetbrains.qodana.cloud.project.QodanaReportDownloader
import org.jetbrains.qodana.report.*
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel


@OptIn(ExperimentalCoroutinesApi::class)
class OpenInIdeCloudReportDescriptor(
  private val linkedDeferred: Deferred<LinkState.Linked>,
  private val authorized: UserState.Authorized,
  val reportId: String,
  val projectId: String,
  @NlsSafe val projectName: String?,
  private val project: Project,
  private val doDownload: Boolean = true,
) : ReportDescriptor {
  override val isReportAvailableFlow: Flow<NotificationCallback?> = flow {
    val linked = linkedDeferred.await()
    emitAll(linkedCloudReportDescriptor(linked).isReportAvailableFlow)
  }

  private val browserViewProvider: BrowserViewProvider = BrowserViewProvider.qodanaCloudReport(projectId, reportId)

  override val browserViewProviderFlow: Flow<BrowserViewProvider> = flowOf(browserViewProvider)

  override val bannerContentProviderFlow: Flow<BannerContentProvider?> = emptyFlow()

  override val noProblemsContentProviderFlow: Flow<NoProblemsContentProvider> = flow {
    emit(NoProblemsContentProviderImpl())
    val linked = linkedDeferred.await()
    emitAll(linkedCloudReportDescriptor(linked).noProblemsContentProviderFlow)
  }

  fun linkedState(): LinkState.Linked? {
    return try {
      linkedDeferred.getCompleted()
    } catch (_ : IllegalStateException) {
      null
    }
  }

  override suspend fun refreshReport(): ReportDescriptor? {
    val linked = linkedState()
    return if (linked != null) linkedCloudReportDescriptor(linked).refreshReport() else this
  }

  override suspend fun loadReport(project: Project): LoadedReport.Sarif? {
    return QodanaReportDownloader.getInstance(project).getReport(authorized, reportId, projectId, doDownload)
  }

  private fun linkedCloudReportDescriptor(linked: LinkState.Linked): LinkedCloudReportDescriptor {
    return LinkedCloudReportDescriptor(linked, reportId, project)
  }

  override fun hashCode(): Int = Objects.hashCode(reportId, projectId)

  override fun equals(other: Any?): Boolean {
    if (other !is OpenInIdeCloudReportDescriptor) return false
    return reportId == other.reportId && projectId == other.projectId
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
      val cloudProjectName = projectName ?: projectId
      return NoProblemsContentProvider.NoProblemsContent(
        title = QodanaBundle.message("no.problems.content.not.matched.cloud.report.title"),
        description = QodanaBundle.message("no.problems.content.not.matched.open.in.ide.report.description", cloudProjectName, totalProblemsCount, project.name),
        actions = NoProblemsContentProvider.openOtherReportAction() to NoProblemsContentProvider.openOtherProjectAction()
      )
    }
  }
}