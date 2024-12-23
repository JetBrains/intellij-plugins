@file:OptIn(FlowPreview::class)

package org.jetbrains.qodana.ui.problemsView.viewModel.impl

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.util.progress.mapWithProgress
import com.intellij.platform.util.progress.reportSequentialProgress
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.highlight.HighlightedReportData
import org.jetbrains.qodana.highlight.InspectionInfoProvider
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.problem.SarifProblemWithProperties
import org.jetbrains.qodana.problem.SarifProblemWithPropertiesAndFile
import org.jetbrains.qodana.problem.navigatable
import org.jetbrains.qodana.report.NoProblemsContentProvider
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.settings.QodanaConfigChangeService
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.toSelectedNodeType
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import org.jetbrains.qodana.ui.problemsView.tree.model.impl.QodanaTreeRootBuilder
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel
import kotlin.coroutines.coroutineContext

internal class UiStateLoadedImpl(
  private val viewModel: QodanaProblemsViewModel,
  private val project: Project,
  private val viewModelScope: CoroutineScope,
  uiStateScope: CoroutineScope,
  private val highlightedReportData: HighlightedReportData,
  initialRoot: QodanaTreeRoot,
  private val reportInteractor: ReportInteractor,
) : QodanaProblemsViewModel.UiState.Loaded {
  private val _treeRootStateFlow = MutableStateFlow(initialRoot)
  override val treeRootStateFlow: StateFlow<QodanaTreeRoot> = _treeRootStateFlow.asStateFlow()

  override val noProblemsContentFlow: Flow<QodanaProblemsViewModel.NoProblemsContent> = createNoProblemsContentFlow()

  override val uiTreeEventsFlow: Flow<QodanaProblemsViewModel.UiTreeEvent> = uiStateScope.qodanaInternalTreeEventsFlow()
    .mapNotNull { processInternalTreeEvent(_treeRootStateFlow, it) }
    .flowOn(QodanaDispatchers.Default)
    .shareIn(uiStateScope, SharingStarted.Lazily)

  override val inspectionInfoProvider: InspectionInfoProvider = highlightedReportData.inspectionsInfoProvider

  private val _selectedTreeNodeFlow: MutableStateFlow<QodanaTreeNode<*, *, *>?> = MutableStateFlow(null)
  override val selectedTreeNodeFlow: Flow<QodanaTreeNode<*, *, *>?> = _selectedTreeNodeFlow.asStateFlow().debounce { it?.let { 0L } ?: 50L }

  override val jobUrl: String?
    get() = highlightedReportData.jobUrl

  init {
    viewModelScope.launch(QodanaDispatchers.Default) {
      selectedTreeNodeFlow.collect { node ->
        node ?: return@collect
        QodanaPluginStatsCounterCollector.PROBLEM_SELECTED.log(
          project,
          node.toSelectedNodeType(),
          node.problemsCount
        )
      }
    }
  }

  override fun treeNodeSelectionChanged(node: QodanaTreeNode<*, *, *>?) {
    _selectedTreeNodeFlow.value = node
  }

  override fun exclude(configExcludeItem: ConfigExcludeItem) {
    viewModelScope.launch(QodanaDispatchers.Default) {
      QodanaConfigChangeService.getInstance(project).excludeData(configExcludeItem)
      highlightedReportData.excludeData(configExcludeItem)
    }
  }

  private fun createNoProblemsContentFlow(): Flow<QodanaProblemsViewModel.NoProblemsContent> {
    return flow {
      val problemsCount = highlightedReportData.allProblems.count()
      val problemsInBaselineCount = highlightedReportData.allProblems.count { it.isInBaseline }
      val problemsNotInBaselineCount = problemsCount - problemsInBaselineCount

      val noProblemsContentProviderFlow = highlightedReportData.sourceReportDescriptor.noProblemsContentProviderFlow
        .map { noProblemsContentProvider ->
          when {
            !highlightedReportData.isMatchingForProject -> {
              noProblemsContentProvider.notMatchingProject(viewModel, problemsCount)
            }
            problemsNotInBaselineCount == 0 && problemsInBaselineCount > 0 -> {
              noProblemsContentProvider.noProblemsWithBaseline(viewModel, problemsInBaselineCount)
            }
            else -> {
              noProblemsContentProvider.noProblems(viewModel)
            }
          }
        }
      emitAll(noProblemsContentProviderFlow)
    }.map { noProblemsContent ->
      val actions = noProblemsContent.actions?.let { actions ->
        val (firstAction, secondAction) = actions
        actionDescriptorOnScope(firstAction) to secondAction?.let { actionDescriptorOnScope(it) }
      }

      QodanaProblemsViewModel.NoProblemsContent(
        noProblemsContent.title,
        noProblemsContent.description,
        actions
      )
    }
  }

  private fun actionDescriptorOnScope(
    action: NoProblemsContentProvider.ActionDescriptor
  ): QodanaProblemsViewModel.NoProblemsContent.ActionDescriptor {
    return QodanaProblemsViewModel.NoProblemsContent.ActionDescriptor(action.text) { component, inputEvent ->
      viewModelScope.launch(QodanaDispatchers.Default) {
        action.action.invoke(component, inputEvent)
      }
    }
  }

  override fun refreshReport() {
    reportInteractor.refresh()
  }

  override fun closeReport() {
    reportInteractor.close()
  }

  private sealed interface InternalTreeEvent {
    class TreeUpdate(val event: QodanaTreeEvent) : InternalTreeEvent

    class ProblemNavigate(val problem: SarifProblem) : InternalTreeEvent
  }

  private fun CoroutineScope.qodanaInternalTreeEventsFlow(): Flow<InternalTreeEvent> {
    val sarifProblemsWithPropertiesFlow = MutableSharedFlow<Set<SarifProblemWithProperties>>()
    launch(QodanaDispatchers.Default) {
      highlightedReportData.updatedProblemsPropertiesFlow.collect {
        sarifProblemsWithPropertiesFlow.emit(it)
      }
    }
    val excludeNodeFlow = highlightedReportData.excludedDataFlow
      .map { QodanaTreeExcludeEvent(it, project) }

    val treeUpdatesFlow = merge(
      excludeNodeFlow,
      sarifProblemsWithPropertiesFlow.map { problems ->
        QodanaTreeProblemEvent(
          problems.map {
            SarifProblemWithPropertiesAndFile(it.problem, it.properties, project)
          }.toSet(),
        )
      }
    )

    return merge(
      treeUpdatesFlow.map { InternalTreeEvent.TreeUpdate(it) },
      highlightedReportData.problemToNavigateFlow.map { InternalTreeEvent.ProblemNavigate(it) }
    )
  }

  private fun processInternalTreeEvent(
    rootStateFlow: MutableStateFlow<QodanaTreeRoot>,
    internalTreeEvent: InternalTreeEvent
  ): QodanaProblemsViewModel.UiTreeEvent? {
    val currentRoot = rootStateFlow.value
    val pathBuilder = QodanaTreePath.Builder()

    return when(internalTreeEvent) {
      is InternalTreeEvent.TreeUpdate -> {
        val newRoot = currentRoot.processTreeEvent(internalTreeEvent.event, pathBuilder)
        newRoot.problemsCount // init lazy problems counter

        //pathBuilder.addNode(newRoot)
        val newPathBuilder = QodanaTreePath.Builder()
        newPathBuilder.addParent(newRoot, pathBuilder.buildPath())
        if (newRoot === currentRoot) return null

        rootStateFlow.value = newRoot

        val path = newPathBuilder.buildPath()
        QodanaProblemsViewModel.UiTreeEvent.Update(path)
      }
      is InternalTreeEvent.ProblemNavigate -> {
        val problemNode = currentRoot.buildPathToProblemNode(internalTreeEvent.problem, pathBuilder) ?: return null
        val path = pathBuilder.buildPath()
        QodanaProblemsViewModel.UiTreeEvent.Navigate(
          path,
          SarifProblemWithProperties(problemNode.primaryData.sarifProblem, problemNode.sarifProblemProperties).navigatable(project)
        )
      }
    }
  }
}

data class QodanaTreeBuildConfiguration(
  val showBaselineProblems: Boolean,
  val groupBySeverity: Boolean,
  val groupByInspection: Boolean,
  val groupByModule: Boolean,
  val groupByDirectory: Boolean
)

suspend fun buildQodanaTreeRootForHighlightedReport(
  project: Project,
  highlightedReportData: HighlightedReportData,
  treeBuildConfiguration: QodanaTreeBuildConfiguration
): QodanaTreeRoot = reportSequentialProgress { reporter ->
  val sarifProblemPropertiesProvider = highlightedReportData.sarifProblemPropertiesProvider.value

  val sarifProblemsWithProperties = sarifProblemPropertiesProvider.problemsWithProperties
    .filter { if (treeBuildConfiguration.showBaselineProblems) true else !it.problem.isInBaseline }
    .toList()

  val sarifProblemsWithVirtualFiles = reporter.nextStep(25, QodanaBundle.message("progress.title.qodana.computing.problems")) {
    computeSarifProblemsWithVirtualFiles(project, sarifProblemsWithProperties)
  }

  val moduleDataProvider = reporter.nextStep(50, QodanaBundle.message("progress.title.qodana.computing.modules")) {
    if (treeBuildConfiguration.groupByModule) {
      ModuleDataProvider.create(project, sarifProblemsWithVirtualFiles.map { it.first.problem to it.second })
    } else {
      null
    }
  }
  val treeContext = QodanaTreeContext(
    treeBuildConfiguration.groupBySeverity,
    treeBuildConfiguration.groupByInspection,
    moduleDataProvider,
    treeBuildConfiguration.groupByDirectory,
    highlightedReportData.createdAt,
    project,
    highlightedReportData.inspectionsInfoProvider
  )
  val rootData = QodanaTreeRoot.PrimaryData(
    reportName = highlightedReportData.reportName,
    branch = highlightedReportData.vcsData.branch,
    revision = highlightedReportData.vcsData.revision,
    createdAt = highlightedReportData.createdAt
  )
  reporter.nextStep(100) {
    QodanaTreeRootBuilder(treeContext, sarifProblemsWithVirtualFiles, highlightedReportData.excludedDataFlow.value, rootData).buildRoot()
  }
}

private suspend fun computeSarifProblemsWithVirtualFiles(
  project: Project,
  sarifProblemsWithProperties: List<SarifProblemWithProperties>
): List<Pair<SarifProblemWithProperties, VirtualFile>> {
  val sarifProblemFileToVirtualFile: Map<String, VirtualFile> = sarifProblemsWithProperties
    .map { it.problem }
    .distinctBy { it.relativePathToFile }
    .withIndex()
    .toList()
    .mapWithProgress { (i, sarifProblem) ->
      coroutineContext.ensureActive()
      if (i % 10000 == 0) yield()

      val virtualFile = sarifProblem.getVirtualFile(project) ?: return@mapWithProgress null
      sarifProblem.relativePathToFile to virtualFile
    }
    .filterNotNull()
    .toMap()

  return sarifProblemsWithProperties.mapNotNull {
    it to (sarifProblemFileToVirtualFile[it.problem.relativePathToFile] ?: return@mapNotNull null)
  }
}