package org.jetbrains.qodana.ui.problemsView.viewModel

import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.project.LinkState
import org.jetbrains.qodana.cloud.project.QodanaCloudProjectDataProvider
import org.jetbrains.qodana.cloud.project.QodanaCloudProjectLinkService
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.highlight.HighlightedReportState
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.highlight.reportDescriptorIfSelectedOrLoading
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.report.BrowserViewProvider
import org.jetbrains.qodana.report.ReportDescriptor
import org.jetbrains.qodana.run.QodanaRunInIdeService
import org.jetbrains.qodana.run.QodanaRunState
import org.jetbrains.qodana.settings.qodanaSettings
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.StatsReportType
import org.jetbrains.qodana.stats.TabState
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.QodanaCIConfigService
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeFileNode
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeInspectionNode
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeProblemNode
import org.jetbrains.qodana.ui.problemsView.viewModel.impl.*
import org.jetbrains.qodana.vcs.trimRevisionString

@OptIn(ExperimentalCoroutinesApi::class)
internal class QodanaProblemsViewModelImpl(private val scope: CoroutineScope, private val project: Project): QodanaProblemsViewModel {
  override val problemsViewState: StateFlow<QodanaProblemsViewState> = QodanaProblemsViewStateService.getInstance(project)
    .problemsViewStateFlow

  private val _showPreviewFlow = MutableStateFlow(false)
  override val showPreviewFlow: StateFlow<Boolean> = _showPreviewFlow.asStateFlow()

  override val uiStateFlow: StateFlow<QodanaProblemsViewModel.UiState> = createUiStateFlow()

  override val browserViewProviderStateFlow: StateFlow<BrowserViewProvider?> = createBrowserViewProviderStateFlow()

  override val bannersContentProvidersFlow: Flow<List<BannerContentProvider>> = createAllBannersContentProvidersFlow()

  override val descriptionPanelContentProviderFlow: Flow<QodanaProblemsViewModel.SecondPanelContent?> = createDescriptionContentProviderFlow()

  private data class TabSelectionChanged(
    val isSelected: Boolean,
    val timeMillis: Long
  )

  private val tabSelectionStatsLoggingChannel = Channel<TabSelectionChanged>()

  init {
    scope.launch(QodanaDispatchers.Default) {
      var timeWasSelectedMillis: Long? = null
      tabSelectionStatsLoggingChannel.consumeAsFlow().collect { tabSelectionChanged ->
        val currentUiState = uiStateFlow.value

        if (tabSelectionChanged.isSelected) {
          timeWasSelectedMillis = tabSelectionChanged.timeMillis
          logPanelSelectedStats(project, currentUiState)
        }
        else {
          val durationWasSelected = timeWasSelectedMillis?.let { tabSelectionChanged.timeMillis - it } ?: return@collect
          timeWasSelectedMillis = null
          logPanelUnselectedStats(project, currentUiState, durationWasSelected)
        }
      }
    }
  }

  override fun tabSelectionChanged(isSelected: Boolean) {
    val timeMillis = System.currentTimeMillis()
    scope.launch(QodanaDispatchers.Default) {
      tabSelectionStatsLoggingChannel.send(TabSelectionChanged(isSelected, timeMillis))
    }
  }

  override fun updateProblemsViewState(update: (QodanaProblemsViewState) -> QodanaProblemsViewState) {
    QodanaProblemsViewStateService.getInstance(project).updateProblemsViewState(update)
  }

  override fun updateShowPreviewFlow(newValue: Boolean) {
    _showPreviewFlow.value = newValue
  }

  private fun createUiStateFlow(): StateFlow<QodanaProblemsViewModel.UiState> {
    val runStateFlow = QodanaRunInIdeService.getInstance(project).runState
    return runStateFlow.flatMapLatest { runState ->
      when (runState) {
        is QodanaRunState.Running -> {
          flowOf(UiStateRunningQodanaImpl(runState))
        }
        is QodanaRunState.NotRunning -> {
          val highlightedReportStateFlow = QodanaHighlightedReportService.getInstance(project).highlightedReportState
          highlightedReportStateFlow.flatMapLatest { highlightedReportState ->
            when (highlightedReportState) {
              HighlightedReportState.NotSelected -> {
                uiStatesFlowOnNotSelectedReport()
              }
              is HighlightedReportState.Loading -> {
                flowOf(UiStateLoadingReportImpl(project, reportInteractor(highlightedReportState.sourceReportDescriptor)))
              }
              is HighlightedReportState.Selected -> {
                uiStatesFlowOnSelectedReport(highlightedReportState)
              }
            }
          }
        }
      }
    }.flowOn(QodanaDispatchers.Default).stateIn(scope, SharingStarted.Eagerly, getInitialUiState())
  }

  private fun getInitialUiState(): QodanaProblemsViewModel.UiState {
    val running = QodanaRunInIdeService.getInstance(project).runState.value as? QodanaRunState.Running
    if (running != null) return UiStateRunningQodanaImpl(running)

    val currentReportDescriptor = QodanaHighlightedReportService.getInstance(project).highlightedReportState.value
      .reportDescriptorIfSelectedOrLoading

    return when {
      currentReportDescriptor != null -> {
        UiStateLoadingReportImpl(project, reportInteractor(currentReportDescriptor))
      }
      else -> {
        val ciFile = QodanaCIConfigService.getInstance(project).presentCIFile.value
        val ciState = uiCiState(ciFile)
        when (val userState = QodanaCloudStateService.getInstance().userState.value) {
          is UserState.NotAuthorized -> {
            UiStateNotAuthorizedImpl(project, scope, ciState)
          }
          is UserState.Authorizing -> UiStateAuthorizingImpl(userState)
          is UserState.Authorized -> {
            val authorizedUiState = AuthorizedUiStateImpl(project, scope, userState)
            when (val linked = QodanaCloudProjectLinkService.getInstance(project).linkState.value) {
              is LinkState.Linked -> {
                val availableReportId = linked.projectDataProvider.latestReportId
                UiStateLinkedImpl(project, scope, linked, ciState, availableReportId, authorizedUiState)
              }
              is LinkState.NotLinked -> {
                UiStateNotLinkedImpl(project, scope, authorizedUiState, ciState)
              }
            }
          }
        }
      }
    }
  }

  private fun uiStatesFlowOnNotSelectedReport(): Flow<QodanaProblemsViewModel.UiState> {
    val userStateFlow = QodanaCloudStateService.getInstance().userState
    val ciConfigFlow = QodanaCIConfigService.getInstance(project).presentCIFile
    return userStateFlow.flatMapLatest { userState ->
      ciConfigFlow.flatMapLatest { ciFile ->
        val ciState = uiCiState(ciFile)
        when(userState) {
          is UserState.NotAuthorized -> {
            flowOf(UiStateNotAuthorizedImpl (project, scope, ciState))
          }
          is UserState.Authorizing -> flowOf(UiStateAuthorizingImpl(userState))
          is UserState.Authorized -> {
            val authorizedUiState = AuthorizedUiStateImpl(project, scope, userState)
            val linkState = QodanaCloudProjectLinkService.getInstance(project).linkState
            linkState.flatMapLatest { linked ->
              when (linked) {
                is LinkState.Linked -> {
                  val availableReportId = linked.projectDataProvider.fetchedReportProperty.propertyState.map {
                    it.lastLoadedValue?.response?.asSuccess()?.reportId
                  }
                  availableReportId.mapLatest { reportId ->
                    UiStateLinkedImpl(project, scope, linked, ciState, reportId, authorizedUiState)
                  }
                }
                is LinkState.NotLinked -> {
                  flowOf(UiStateNotLinkedImpl(project, scope, authorizedUiState, ciState))
                }
              }
            }
          }
        }
      }
    }
  }

  private fun uiCiState(ciFile: CIFile?): QodanaProblemsViewModel.CiState {
    return if (ciFile is CIFile.ExistingWithQodana) {
      QodanaProblemsViewModel.CiState.Present(ciFile)
    } else {
      QodanaProblemsViewModel.CiState.NotPresent(project)
    }
  }

  private fun uiStatesFlowOnSelectedReport(selected: HighlightedReportState.Selected): Flow<QodanaProblemsViewModel.UiState> {
    val reportDescriptor = selected.highlightedReportData.sourceReportDescriptor
    return problemsViewState
      .map { it.toTreeBuildConfiguration() }
      .distinctUntilChanged()
      .transformLatest { treeConfiguration ->
        val reportInteractor = reportInteractor(reportDescriptor)
        val loadingReportState = UiStateLoadingReportImpl(project, reportInteractor)
        emit(loadingReportState)
        supervisorScope {
          val highlightedReportData = selected.highlightedReportData

          val root = try {
            async {
              withBackgroundProgress(project, QodanaBundle.message("progress.title.qodana.collecting.problems")) {
                buildQodanaTreeRootForHighlightedReport(project, highlightedReportData, treeConfiguration)
              }
            }.await()
          } catch (e: CancellationException) {
            if (coroutineContext.job.isCancelled) throw e
            loadingReportState.cancel()
            return@supervisorScope
          }

          val loadedReportState = UiStateLoadedImpl(
            viewModel = this@QodanaProblemsViewModelImpl,
            project = project,
            viewModelScope = scope,
            uiStateScope = this@supervisorScope,
            highlightedReportData = highlightedReportData,
            initialRoot = root,
            reportInteractor = reportInteractor
          )
          emit(loadedReportState)
          awaitCancellation()
        }
      }
  }

  private fun createBrowserViewProviderStateFlow(): StateFlow<BrowserViewProvider?> {
    val highlightedReportState = QodanaHighlightedReportService.getInstance(project).highlightedReportState
    return highlightedReportState
      .transformLatest {
        emit(null)
        emitAll(it.reportDescriptorIfSelectedOrLoading?.browserViewProviderFlow ?: flowOf(null))
      }
      .flowOn(QodanaDispatchers.Default)
      .stateIn(scope, SharingStarted.Eagerly, null)
  }

  private fun createAllBannersContentProvidersFlow(): Flow<List<BannerContentProvider>> {
    return combine(
      merge(flowOf(null), createNewCloudReportBannerContentProviderFlow()),
      merge(flowOf(null), createReportBannerContentProviderFlow())
    ) { a, b -> listOfNotNull(a, b) }.distinctUntilChanged()
  }

  private fun createReportBannerContentProviderFlow(): Flow<BannerContentProvider?> {
    val highlightedReportStateFlow = QodanaHighlightedReportService.getInstance(project).highlightedReportState
    return highlightedReportStateFlow
      .transformLatest { highlightedReportState ->
        emit(null)
        val reportDescriptor = highlightedReportState.reportDescriptorIfSelectedOrLoading ?: return@transformLatest
        emitAll(reportDescriptor.bannerContentProviderFlow)
      }
  }

  private fun createNewCloudReportBannerContentProviderFlow(): Flow<BannerContentProvider?> {
    val linkStateFlow = QodanaCloudProjectLinkService.getInstance(project).linkState
    return linkStateFlow.flatMapLatest { linkState ->
      when (linkState) {
        is LinkState.NotLinked -> {
          flowOf(null)
        }
        is LinkState.Linked -> {
          linkState.projectDataProvider.latestReportForNotificationFlow.flatMapLatest { newReport ->
            newReport?.let { newReportBannerContentProvider(linkState, newReport) } ?: flowOf(null)
          }
        }
      }
    }
  }

  private fun newReportBannerContentProvider(linked: LinkState.Linked, newReport: QodanaCloudProjectDataProvider.CloudReport): Flow<BannerContentProvider> {
    val qodanaSettings = project.qodanaSettings()
    return qodanaSettings.loadMatchingCloudReportAutomatically.map { autoLoadEnabled ->
      val loadReportAction = BannerContentProvider.Action(QodanaBundle.message("qodana.cloud.new.report.action.load")) {
        val cloudReportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptorWithId(newReport.reportId)
        QodanaHighlightedReportService.getInstance(project).highlightReport(cloudReportDescriptor)
        logHighlightCloudReportFromNotificationStats(project)
      }
      val alwaysLoadLatestReportAction = if (!autoLoadEnabled) {
        BannerContentProvider.Action(QodanaBundle.message("qodana.cloud.new.report.auto.load")) {
          loadReportAction.callback.invoke()
          qodanaSettings.setLoadMatchingCloudReportAutomatically(true)
        }
      } else null

      val bannerText = newReport.revision?.let { QodanaBundle.message("qodana.cloud.report.from.commit.available", it.trimRevisionString()) }
                       ?: QodanaBundle.message("qodana.cloud.new.report.appeared.text")
      BannerContentProvider(bannerText, listOfNotNull(loadReportAction, alwaysLoadLatestReportAction), onClose = {
        scope.launch(QodanaDispatchers.Default) {
          linked.projectDataProvider.setLatestNoNotificationReportId(newReport.reportId)
        }
      })
    }
  }

  private fun createDescriptionContentProviderFlow(): Flow<QodanaProblemsViewModel.SecondPanelContent?> {
    return uiStateFlow.transformLatest { state ->
      when (state) {
        is QodanaProblemsViewModel.UiState.Loaded -> {
          emitAll(showPreviewFlow.transformLatest { showPreview ->
            emitAll(state.selectedTreeNodeFlow.mapLatest {
              val primaryData = it?.primaryData ?:return@mapLatest null
              when (primaryData) {
                is QodanaTreeInspectionNode.PrimaryData -> {
                  val inspectionId = primaryData.inspectionId
                  val uiState = uiStateFlow.value as? QodanaProblemsViewModel.UiState.Loaded ?: return@mapLatest null
                  QodanaProblemsViewModel.DescriptionPanelContent.createFromId(inspectionId, uiState.inspectionInfoProvider)
                }
                is QodanaTreeProblemNode.PrimaryData -> {
                  if (showPreview) {
                    QodanaProblemsViewModel.EditorPanelContent(it)
                  } else {
                    val inspectionId = primaryData.sarifProblem.inspectionId
                    val uiState = uiStateFlow.value as? QodanaProblemsViewModel.UiState.Loaded ?: return@mapLatest null
                    QodanaProblemsViewModel.DescriptionPanelContent.createFromId(inspectionId, uiState.inspectionInfoProvider)
                  }
                }
                is QodanaTreeFileNode.PrimaryData -> {
                  if (showPreview) QodanaProblemsViewModel.EditorPanelContent(it)
                  else null
                }
                else -> null
              }
            })
          })
        }
        else -> emit(null)
      }
    }.distinctUntilChanged().flowOn(QodanaDispatchers.Default)
  }

  private fun reportInteractor(reportDescriptor: ReportDescriptor): ReportInteractor {
    return ReportInteractor(project, scope, reportDescriptor)
  }
}

private fun logHighlightCloudReportFromNotificationStats(project: Project) {
  QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
    project,
    true,
    StatsReportType.CLOUD,
    SourceHighlight.CLOUD_HIGHLIGHT_NEW_REPORT_APPEARED_NOTIFICATION
  )
}

private fun logPanelSelectedStats(project: Project, uiState: QodanaProblemsViewModel.UiState) {
  QodanaPluginStatsCounterCollector.TAB_SELECTED.log(project, uiState.toUiStateStatsType())
}

private fun logPanelUnselectedStats(project: Project, uiState: QodanaProblemsViewModel.UiState, durationMillis: Long) {
  QodanaPluginStatsCounterCollector.TAB_UNSELECTED.log(project, uiState.toUiStateStatsType(), durationMillis)
}

internal fun QodanaProblemsViewModel.UiState.toUiStateStatsType(): TabState {
  return when(this) {
    is QodanaProblemsViewModel.UiState.Loaded -> TabState.SELECTED_REPORT
    is QodanaProblemsViewModel.UiState.LoadingReport -> TabState.LOADING_REPORT
    is QodanaProblemsViewModel.UiState.RunningQodana -> TabState.ANALYZING
    is QodanaProblemsViewModel.UiState.Authorizing -> TabState.AUTHORIZING
    is QodanaProblemsViewModel.UiState.Linked -> {
      val isCiPresent = ciState is QodanaProblemsViewModel.CiState.Present
      if (isCiPresent) TabState.AUTHORIZED_LINKED_CI_PRESENT else TabState.AUTHORIZED_LINKED_NO_CI
    }
    is QodanaProblemsViewModel.UiState.NotLinked -> {
      val isCiPresent = ciState is QodanaProblemsViewModel.CiState.Present
      if (isCiPresent) TabState.AUTHORIZED_NOT_LINKED_PRESENT else TabState.AUTHORIZED_NOT_LINKED_NO_CI
    }
    is QodanaProblemsViewModel.UiState.NotAuthorized -> {
      val isCiPresent = ciState is QodanaProblemsViewModel.CiState.Present
      if (isCiPresent) TabState.NOT_AUTHORIZED_CI_PRESENT else TabState.NOT_AUTHORIZED_NO_CI
    }
  }
}

private fun QodanaProblemsViewState.toTreeBuildConfiguration(): QodanaTreeBuildConfiguration {
  return QodanaTreeBuildConfiguration(
    this.showBaselineProblems,
    this.groupBySeverity,
    this.groupByInspection,
    this.groupByModule,
    this.groupByDirectory
  )
}
