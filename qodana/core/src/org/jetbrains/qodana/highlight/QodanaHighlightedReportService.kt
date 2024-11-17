package org.jetbrains.qodana.highlight

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.components.*
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportSequentialProgress
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.project.LinkState
import org.jetbrains.qodana.cloud.project.LinkedCloudReportDescriptor
import org.jetbrains.qodana.cloud.project.QodanaCloudProjectLinkService
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.protocol.OpenInIdeCloudReportDescriptor
import org.jetbrains.qodana.report.FileReportDescriptor
import org.jetbrains.qodana.report.LoadedReport
import org.jetbrains.qodana.report.ReportDescriptor
import org.jetbrains.qodana.run.LocalRunNotPublishedReportDescriptor
import org.jetbrains.qodana.run.LocalRunPublishedReportDescriptor
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.toStatsReportType
import org.jetbrains.qodana.ui.problemsView.QodanaProblemsViewTab
import kotlin.io.path.Path
import kotlin.io.path.pathString

/**
 * Service responsible for state of highlighted report, see [HighlightedReportState]
 *
 * See [highlightedReportState], use [highlightReport] to highlight new report (or unhighlight).
 * Observes currently highlighted report availability, in case if it's not available, unhighlights it.
 */
@State(name = "QodanaHighlightedReportService", storages = [Storage(value = StoragePathMacros.WORKSPACE_FILE)])
@Service(Service.Level.PROJECT)
class QodanaHighlightedReportService(private val project: Project, private val scope: CoroutineScope)
  : PersistentStateComponent<QodanaHighlightedReportService.State> {
  companion object {
    fun getInstance(project: Project): QodanaHighlightedReportService = project.service()

    fun getInstanceIfCreated(project: Project): QodanaHighlightedReportService? {
      return if (QodanaIsSelectedPersistenceService.getInstance(project).isSelectedOrLoading) project.service() else project.serviceIfCreated()
    }
  }

  private val _highlightedReportState = MutableStateFlow<HighlightedReportState>(HighlightedReportState.NotSelected)
  val highlightedReportState: StateFlow<HighlightedReportState> = _highlightedReportState.asStateFlow()


  private class ReportToHighlightRequest(
    val reportDescriptor: ReportDescriptor?,
    val forceReload: Boolean,
    val uiTabsActivateRequest: UiTabsActivateRequest,
    val result: CompletableDeferred<HighlightedReportState.Selected?>
  )

  data class UiTabsActivateRequest(
    val activateServerSideAnalysis: Boolean,
    val activateCoverage: Boolean
  )

  private val _uiTabsActivateRequest = MutableSharedFlow<UiTabsActivateRequest>(replay = 1)
  val uiTabsActivateRequest: SharedFlow<UiTabsActivateRequest> = _uiTabsActivateRequest.asSharedFlow()

  private val reportToHighlightEvent = Channel<ReportToHighlightRequest>()

  private class HighlightedReportStateSelectedImpl(
    override val highlightedReportData: HighlightedReportDataImpl,
  ) : HighlightedReportState.Selected

  private class NewReportLoadingState(override val sourceReportDescriptor: ReportDescriptor) : HighlightedReportState.Loading

  private class PersistedReportLoadingState(override val sourceReportDescriptor: ReportDescriptor) : HighlightedReportState.Loading

  suspend fun highlightReport(
    reportDescriptor: ReportDescriptor?,
    withFocus: Boolean = true,
    activateCoverage: Boolean = false,
    forceReload: Boolean = false,
  ): HighlightedReportState.Selected? {
    QodanaHighlightingListener.callOnStart(project)
    reportToHighlightEventSubscription.start()
    highlightedReportAvailabilitySubscription.start()
    highlightedReportStateUiUpdatesSubscription.start()
    highlightedReportSubscription.start()
    highlightedReportInfoStatsSubscription.start()

    val result = CompletableDeferred<HighlightedReportState.Selected?>()
    val request = ReportToHighlightRequest(
      reportDescriptor,
      forceReload,
      UiTabsActivateRequest(activateServerSideAnalysis = withFocus, activateCoverage = activateCoverage),
      result
    )
    reportToHighlightEvent.send(request)

    return result.await()
  }

  private val reportToHighlightEventSubscription = scope.launch(QodanaDispatchers.Default, CoroutineStart.LAZY) {
    reportToHighlightEvent.receiveAsFlow().collectLatest { reportToHighlightRequest ->
      val reportDescriptor = reportToHighlightRequest.reportDescriptor
      val forceReload = reportToHighlightRequest.forceReload

      var finalState: HighlightedReportState? = null

      try {
        yield()
        supervisorScope {
          launch highlight@{
            withBackgroundProgress(project, QodanaBundle.message("progress.title.qodana.loading.report")) {
              reportSequentialProgress { reporter ->
                val updatedReportState = transitionReportStateToLoading(reportDescriptor, forceReload)
                if (updatedReportState !is HighlightedReportState.Loading) {
                  if (updatedReportState is HighlightedReportState.Selected &&
                      updatedReportState.highlightedReportData.sourceReportDescriptor == reportDescriptor) {
                    finalState = updatedReportState
                  }
                  return@withBackgroundProgress
                }

                finalState = HighlightedReportState.NotSelected

                var loadingReportDescriptor = updatedReportState.sourceReportDescriptor
                var loadedReport: LoadedReport? = LoadedReport.Delegate(loadingReportDescriptor)
                reporter.nextStep(70) {
                  while (true) {
                    checkCanceled()
                    val delegateReport = loadedReport as? LoadedReport.Delegate ?: break

                    loadingReportDescriptor = delegateReport.reportDescriptorDelegate
                    loadedReport = delegateReport.reportDescriptorDelegate.loadReport(project)
                    _highlightedReportState.value = NewReportLoadingState(loadingReportDescriptor)
                  }
                }

                val highlightedReportData = when(loadedReport) {
                  is LoadedReport.Sarif -> reporter.nextStep(100, QodanaBundle.message("progress.title.qodana.reading.report")) {
                    HighlightedReportDataImpl.create(project, loadingReportDescriptor, loadedReport as LoadedReport.Sarif)
                  }
                  is LoadedReport.Delegate -> throw IllegalStateException("Delegate must be processed before")
                  null -> return@withBackgroundProgress
                }

                finalState = HighlightedReportStateSelectedImpl(highlightedReportData)
              }
            }
          }
        }
      } finally {
        val result = finalState
        if (result != null) {
          _highlightedReportState.value = result
        }

        val selected = result as? HighlightedReportState.Selected
        reportToHighlightRequest.result.complete(selected)
        if (selected != null) {
          _uiTabsActivateRequest.emit(reportToHighlightRequest.uiTabsActivateRequest)
        }
      }
    }
  }

  private fun transitionReportStateToLoading(reportToLoad: ReportDescriptor?, forceReload: Boolean): HighlightedReportState {
    return _highlightedReportState.updateAndGet { currentState ->
      if (!forceReload && !isUpdateNeeded(currentState, reportToLoad)) return currentState

      reportToLoad?.let { NewReportLoadingState(it) } ?: HighlightedReportState.NotSelected
    }
  }

  private fun isUpdateNeeded(currentState: HighlightedReportState, newReportDescriptor: ReportDescriptor?): Boolean {
    return when(currentState) {
      is HighlightedReportState.Selected -> currentState.highlightedReportData.sourceReportDescriptor != newReportDescriptor
      is NewReportLoadingState -> currentState.sourceReportDescriptor != newReportDescriptor
      is PersistedReportLoadingState -> true
      HighlightedReportState.NotSelected -> newReportDescriptor != null
      else -> error("Invalid state $currentState")
    }
  }

  private val highlightedReportAvailabilitySubscription = scope.launch(QodanaDispatchers.Default, CoroutineStart.LAZY) {
    _highlightedReportState.collectLatest { highlightedReportState ->
      supervisorScope {
        launch unhighlight@{
          if (highlightedReportState !is HighlightedReportStateSelectedImpl) return@unhighlight
          val highlightedReportData = highlightedReportState.highlightedReportData
          try {
            val notificationCallback = highlightedReportData.sourceReportDescriptor.isReportAvailableFlow.first()
            val reportDescriptor = highlightedReportData.sourceReportDescriptor
            logUnhighlightWhenNotAvailableStats(reportDescriptor)
            notificationCallback?.invoke()
            highlightReport(null)
          }
          catch (e: NoSuchElementException) {
            return@unhighlight
          }
        }
      }
    }
  }

  private fun logUnhighlightWhenNotAvailableStats(reportDescriptor: ReportDescriptor) {
    QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
      project,
      false,
      reportDescriptor.toStatsReportType(),
      SourceHighlight.REPORT_NOT_AVAILABLE
    )
  }

  private val highlightedReportStateUiUpdatesSubscription = scope.launch(QodanaDispatchers.Ui, CoroutineStart.LAZY) {
    supervisorScope {
      launch {
        highlightedReportState.collect {
          DaemonCodeAnalyzer.getInstance(project).restart()
        }
      }
      launch {
        uiTabsActivateRequest
          .map { it.activateServerSideAnalysis }
          .collect { activateServerSideAnalysis ->
            if (activateServerSideAnalysis) {
              QodanaProblemsViewTab.show(project)
            }
            else {
              QodanaProblemsViewTab.initView(project)
            }
          }
      }
    }
  }

  private val highlightedReportSubscription = scope.launch(QodanaDispatchers.Default, CoroutineStart.LAZY) {
    launch {
      highlightedReportState.filterIsInstance<HighlightedReportStateSelectedImpl>().collectLatest {
        it.highlightedReportData.processEvents()
      }
    }
    launch {
      highlightedReportState.collect {
        QodanaIsSelectedPersistenceService.getInstance(project).isSelectedOrLoading = it !is HighlightedReportState.NotSelected
      }
    }
  }

  private val highlightedReportInfoStatsSubscription = scope.launch(QodanaDispatchers.Default, CoroutineStart.LAZY) {
    highlightedReportState.filterIsInstance<HighlightedReportState.Selected>().collectLatest {
      QodanaPluginStatsCounterCollector.HIGHLIGHTED_REPORT_INFO.log(
        project,
        it.highlightedReportData.sourceReportDescriptor.toStatsReportType(),
        it.highlightedReportData.allProblems.count(),
      )
    }
  }

  class State : BaseState() {
    var localReportPersistedInfo by property<LocalReportPersistedInfo?>(null)

    var cloudReportPersistedInfo by property<CloudReportPersistedInfo?>(null)

    var openInIdeCloudReportPersistedInfo by property<OpenInIdeCloudReportPersistedInfo?>(null)

    var localRunNotPublishedPersistedInfo by property<LocalReportPersistedInfo?>(null)

    var localRunPublishedPersistedInfo by property<LocalRunPublishedPersistedInfo?>(null)
  }

  class LocalReportPersistedInfo : BaseState() {
    var reportGuid: String? by string()

    var reportName: String? by string()

    var isQodana: Boolean by property(true)

    var path: String? by string()
  }

  class CloudReportPersistedInfo : BaseState() {
    var reportId: String? by string()

    var projectId: String? by string()
  }

  class OpenInIdeCloudReportPersistedInfo : BaseState() {
    var reportId: String? by string()

    var projectId: String? by string()

    var projectName: String? by string()
  }

  class LocalRunPublishedPersistedInfo : BaseState() {
    var reportLink: String? by string()

    var fileReportPersistedInfo by property<LocalReportPersistedInfo?>(null)
  }

  override fun getState(): State {
    val data = highlightedReportState.value.highlightedReportDataIfSelected
    return when (val descriptor = data?.sourceReportDescriptor) {
      is FileReportDescriptor -> {
        State().apply {
          localReportPersistedInfo = persistedInfoFromFileReport(descriptor)
        }
      }
      is LinkedCloudReportDescriptor -> {
        State().apply {
          cloudReportPersistedInfo = CloudReportPersistedInfo().apply {
            reportId = descriptor.reportId
            projectId = descriptor.projectId
          }
        }
      }
      is OpenInIdeCloudReportDescriptor -> {
        val linkedState = descriptor.linkedState()
        if (linkedState != null) {
          State().apply {
            cloudReportPersistedInfo = CloudReportPersistedInfo().apply {
              reportId = descriptor.reportId
              projectId = descriptor.projectId
            }
          }
        } else {
          State().apply {
            openInIdeCloudReportPersistedInfo = OpenInIdeCloudReportPersistedInfo().apply {
              reportId = descriptor.reportId
              projectId = descriptor.projectId
              projectName = descriptor.projectName
            }
          }
        }
      }
      is LocalRunNotPublishedReportDescriptor -> {
        State().apply {
          localRunNotPublishedPersistedInfo = persistedInfoFromFileReport(descriptor.fileReportDescriptor)
        }
      }
      is LocalRunPublishedReportDescriptor -> {
        State().apply {
          localRunPublishedPersistedInfo = LocalRunPublishedPersistedInfo().apply {
            reportLink = descriptor.publishedReportLink
            fileReportPersistedInfo = persistedInfoFromFileReport(descriptor.fileReportDescriptor)
          }
        }
      }
      else -> State()
    }
  }

  override fun loadState(state: State) {
    val localReportPersistedInfo = state.localReportPersistedInfo
    val cloudReportPersistedInfo = state.cloudReportPersistedInfo
    val openInIdeCloudReportPersistedInfo = state.openInIdeCloudReportPersistedInfo
    val localRunNotPublishedReportPersistedInfo = state.localRunNotPublishedPersistedInfo
    val localRunPublishedPersistedInfo = state.localRunPublishedPersistedInfo

    QodanaIsSelectedPersistenceService.getInstance(project).isSelectedOrLoading = false
    val reportDescriptor: ReportDescriptor = when {
      localReportPersistedInfo != null -> {
        val fileReportDescriptor = fileReportDescriptorFromPersisted(localReportPersistedInfo) ?: return
        fileReportDescriptor
      }
      cloudReportPersistedInfo != null -> {
        val projectId = cloudReportPersistedInfo.projectId ?: return
        val reportId = cloudReportPersistedInfo.reportId ?: return

        val linked = QodanaCloudProjectLinkService.getInstance(project).linkState.value as? LinkState.Linked ?: return
        if (linked.projectDataProvider.projectPrimaryData.id != projectId) return

        LinkedCloudReportDescriptor(linked, reportId, project, doDownload = false)
      }
      openInIdeCloudReportPersistedInfo != null -> {
        val reportId = openInIdeCloudReportPersistedInfo.reportId ?: return
        val projectId = openInIdeCloudReportPersistedInfo.projectId ?: return
        val projectName = openInIdeCloudReportPersistedInfo.projectName
        val authorized = QodanaCloudStateService.getInstance().userState.value as? UserState.Authorized ?: return

        OpenInIdeCloudReportDescriptor(linkedDeferred = CompletableDeferred(), authorized, reportId, projectId, projectName, project, doDownload = false)
      }
      localRunNotPublishedReportPersistedInfo != null -> {
        val fileReportDescriptor = fileReportDescriptorFromPersisted(localRunNotPublishedReportPersistedInfo) ?: return
        LocalRunNotPublishedReportDescriptor(fileReportDescriptor, notificationIfFileNotPresent = false)
      }
      localRunPublishedPersistedInfo != null -> {
        val reportLink = localRunPublishedPersistedInfo.reportLink ?: return
        val persistedInfo = localRunPublishedPersistedInfo.fileReportPersistedInfo ?: return
        val fileReportDescriptor = fileReportDescriptorFromPersisted(persistedInfo) ?: return
        LocalRunPublishedReportDescriptor(fileReportDescriptor, reportLink, notificationIfFileNotPresent = false)
      }
      else -> return
    }
    _highlightedReportState.value = PersistedReportLoadingState(reportDescriptor)
    scope.launch(QodanaDispatchers.Default) { highlightReport(reportDescriptor, withFocus = false) }
  }

  private fun fileReportDescriptorFromPersisted(persisted: LocalReportPersistedInfo): FileReportDescriptor? {
    val reportGuid = persisted.reportGuid ?: return null
    val path = persisted.path?.let { Path(it) } ?: return null
    val reportName = persisted.reportName ?: return null
    val isQodana = persisted.isQodana
    return FileReportDescriptor(path, isQodana, reportGuid, reportName, project)
  }

  private fun persistedInfoFromFileReport(fileReportDescriptor: FileReportDescriptor): LocalReportPersistedInfo {
    return LocalReportPersistedInfo().apply {
      reportGuid = fileReportDescriptor.reportGuid
      reportName = fileReportDescriptor.reportName
      path = fileReportDescriptor.reportPath.pathString
      isQodana = fileReportDescriptor.isQodanaReport
    }
  }
}
