package org.jetbrains.qodana.protocol

import com.intellij.openapi.project.Project
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.report.*
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel
import java.util.*

class SingleMarkerReportDescriptor(
  private val project: Project,
  private val sarif: ValidatedSarif,
  private val pathText: String,
  private val reportRevision: String?,
  private val origin: String,
  private val message: String,
) : LocalReportDescriptor {
  private val reportUnavailableDeferred = CompletableDeferred<Unit>()

  override val isReportAvailableFlow: Flow<NotificationCallback?>
    get() {
      return flow {
        reportUnavailableDeferred.await()
        emit(null)
      }
    }

  override val browserViewProviderFlow: Flow<BrowserViewProvider> = emptyFlow()

  override val bannerContentProviderFlow: Flow<BannerContentProvider?> = emptyFlow()

  override val noProblemsContentProviderFlow: Flow<NoProblemsContentProvider> = flowOf(NoProblemsContentProviderImpl())

  override suspend fun refreshReport(): ReportDescriptor = this

  override fun checkAvailability(): Boolean = !reportUnavailableDeferred.isCompleted

  override fun markAsUnavailable() {
    reportUnavailableDeferred.complete(Unit)
  }

  override fun getName(highlightedState: LocalReportDescriptor.HighlightedState): String = "$pathText - $message"

  override suspend fun loadReport(project: Project): LoadedReport.Sarif {
    return LoadedReport.Sarif(sarif, AggregatedReportMetadata(emptyMap()), pathText)
  }

  override fun hashCode(): Int = Objects.hash(pathText, origin, reportRevision)

  override fun equals(other: Any?): Boolean {
    if (other !is SingleMarkerReportDescriptor) return false

    return (pathText == other.pathText && origin == other.origin && reportRevision == other.reportRevision)
  }

  private inner class NoProblemsContentProviderImpl : NoProblemsContentProvider {
    private val noProblemsContent = NoProblemsContentProvider.NoProblemsContent(
      title = QodanaBundle.message("no.problems.content.show.marker.title"),
      description = QodanaBundle.message("no.problems.content.show.marker.description", pathText, project.name),
      actions = null
    )

    override fun noProblems(qodanaProblemsViewModel: QodanaProblemsViewModel): NoProblemsContentProvider.NoProblemsContent {
      return noProblemsContent
    }

    override fun notMatchingProject(
      qodanaProblemsViewModel: QodanaProblemsViewModel,
      totalProblemsCount: Int
    ): NoProblemsContentProvider.NoProblemsContent {
      return noProblemsContent
    }
  }
}