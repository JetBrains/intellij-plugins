package org.jetbrains.qodana.ui.problemsView

import com.intellij.analysis.problemsView.toolWindow.Node
import com.intellij.analysis.problemsView.toolWindow.ProblemsViewPanel
import com.intellij.analysis.problemsView.toolWindow.ProblemsViewState
import com.intellij.ide.OccurenceNavigator
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.getPreferredFocusedComponent
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.platform.util.coroutines.childScope
import com.intellij.profile.codeInspection.ui.DescriptionEditorPane
import com.intellij.profile.codeInspection.ui.SingleInspectionProfilePanel
import com.intellij.profile.codeInspection.ui.readHTMLWithCodeHighlighting
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.panels.Wrapper
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.JBUI.Fonts
import com.intellij.util.ui.StatusText
import com.intellij.util.ui.tree.TreeUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.concurrency.asDeferred
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreePath
import org.jetbrains.qodana.ui.problemsView.tree.model.openFileDescriptor
import org.jetbrains.qodana.ui.problemsView.tree.ui.*
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel
import org.jetbrains.qodana.ui.setContentAndRepaint
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.InputEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.TreePath
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class QodanaProblemsViewPanel(
  scope: CoroutineScope,
  val viewModel: QodanaProblemsViewModel,
  id: String,
  project: Project,
  state: ProblemsViewState
) : ProblemsViewPanel(project, id, state, { QodanaBundle.message("problems.toolwindow.qodana.panel.name") }), OccurenceNavigator {
  companion object {
    val DATA_KEY: DataKey<QodanaProblemsViewPanel> = DataKey.create("QodanaProblemsViewPanel")
  }

  private val root = QodanaUiTreeRoot(
    this,
    modelRootProvider = { (viewModel.uiStateFlow.value as? QodanaProblemsViewModel.UiState.Loaded)?.treeRootStateFlow?.value }
  )

  private val problemsNavigator = QodanaProblemsViewNavigator(this, scope)

  private lateinit var centerComponentWrapper: Wrapper

  val toolbarInsets: Insets?
    get() {
      val toolbarComponent = myToolbar.component
      return toolbarComponent.border.getBorderInsets(toolbarComponent)
    }

  override fun updatePreview() {}

  init {
    val uiState = viewModel.uiStateFlow.value
    val isSplitterResizeEnabled = (uiState is QodanaProblemsViewModel.UiState.Loaded)
    setResizeEnabled(isSplitterResizeEnabled)

    val initialScope = scope.childScope()
    centerComponentWrapper.setContent(getViewForUiState(uiState, initialScope))
    treeModel.root = root
    tree.showsRootHandles = false

    scope.launch(QodanaDispatchers.Ui) {
      viewModel.uiStateFlow
        .debounce {
          if (it is QodanaProblemsViewModel.UiState.Loaded) ZERO else 200.milliseconds
        }
        .collectLatest { uiState ->
          var listener: TreeSelectionListener? = null
          try {
            coroutineScope {
              initialScope.cancel()
              val view = getViewForUiState(uiState, this)
              centerComponentWrapper.setContentAndRepaint(view)
              IdeFocusManager.getInstance(project).requestFocusInProject(view.getPreferredFocusedComponent() ?: view, project)

              treeStructureChanged()
              setResizeEnabled(false)

              val emptyText = tree.emptyText
              when (uiState) {
                is QodanaProblemsViewModel.UiState.Loaded -> {
                  listener = TreeSelectionListener {
                    val uiNode = it.newLeadSelectionPath?.lastPathComponent as? QodanaUiTreeNode<*, *>
                    uiState.treeNodeSelectionChanged(uiNode?.modelTreeNode)
                  }
                  tree.addTreeSelectionListener(listener)

                  setResizeEnabled(true)
                  emptyText.clear()
                  launch {
                    uiState.noProblemsContentFlow.collect { noProblemsContent ->
                      updateStatusText(emptyText, noProblemsContent)
                    }
                  }
                  expandFirstNodesWithoutChildren()

                  val updatesFlow = uiState.uiTreeEventsFlow
                  launch(QodanaDispatchers.Default) {
                    merge(
                      updatesFlow.filterIsInstance<QodanaProblemsViewModel.UiTreeEvent.Update>().conflateUpdatesOfPathsWithSamePrefix(),
                      updatesFlow.filterIsInstance<QodanaProblemsViewModel.UiTreeEvent.Navigate>()
                    ).collect {
                      when (it) {
                        is QodanaProblemsViewModel.UiTreeEvent.Update -> processTreeUpdateEvent(it)
                        is QodanaProblemsViewModel.UiTreeEvent.Navigate -> processTreeNavigateEvent(it)
                      }
                    }
                  }
                }
                else -> {}
              }
            }
          } catch (e: CancellationException) {
            tree.removeTreeSelectionListener(listener)
            listener = null
            throw e
          }
        }
    }
    scope.launch(QodanaDispatchers.Ui) {
      viewModel.uiStateFlow.collectLatest {
        if (it is QodanaProblemsViewModel.UiState.Loaded) {
          it.treeRootStateFlow.collectLatest { root ->
            tree.isRootVisible = !root.children.nodesSequence.none()
          }
        }
      }
    }
  }

  override fun createCenterComponent(): JComponent {
    centerComponentWrapper = Wrapper()
    return centerComponentWrapper
  }

  override fun selectionChangedTo(selected: Boolean) {
    viewModel.tabSelectionChanged(selected)
    super.selectionChangedTo(selected)
  }

  private fun getViewForUiState(uiState: QodanaProblemsViewModel.UiState, scope: CoroutineScope): JComponent {
    return when(uiState) {
      is QodanaProblemsViewModel.UiState.Loaded -> {
        setupDescriptionView(scope)
        tree
      }
      else -> {
        qodanaPanelViewIfNotLoaded(uiState) ?: tree
      }
    }
  }

  private fun setupDescriptionView(scope: CoroutineScope) {
    val descriptionPanel = DescriptionPanel()
    scope.launch(QodanaDispatchers.Ui) {
      try {
        viewModel.descriptionPanelContentProviderFlow.collect {
          secondComponent = when (it) {
            is QodanaProblemsViewModel.EditorPanelContent -> {
              val modelTreeNode = it.modelTreeNode
              val uiTreeNode = tree.selectionPath?.lastPathComponent as? QodanaUiTreeNode<*, *>
              if (uiTreeNode?.primaryData != modelTreeNode.primaryData) {
                null
              } else {
                val openFileDescriptor = modelTreeNode.openFileDescriptor(project)
                myPreview.open(openFileDescriptor)
                myPreview.editor()?.component
              }
            }
            is QodanaProblemsViewModel.DescriptionPanelContent -> {
              descriptionPanel.setInspectionId(it.inspectionId)
              descriptionPanel.setDescription(it.info)
              descriptionPanel.panel
            }
            null -> null
          }
        }
      } catch (e: CancellationException) {
        secondComponent = null
        throw e
      }
    }
  }

  private inner class DescriptionPanel {
    val panel = JPanel(GridBagLayout())

    private val myDescription = DescriptionEditorPane()
    private val label = JBLabel()
    init {
      myDescription.addHyperlinkListener(SingleInspectionProfilePanel.createSettingsHyperlinkListener(project))
      val scrollPane = JBScrollPane(myDescription).apply {
        border = JBUI.Borders.empty(4)
      }
      label.apply {
        border = JBUI.Borders.empty(4)
        font = Fonts.label().asBold()
      }
      val gc = GridBagConstraints()
      gc.weightx = 1.0
      gc.gridx = 0
      gc.gridy = 0
      gc.fill = GridBagConstraints.HORIZONTAL
      panel.add(label, gc)
      gc.weighty = 1.0
      gc.gridx = 0
      gc.gridy = 1
      gc.fill = GridBagConstraints.BOTH
      panel.add(scrollPane, gc)
    }

    fun setInspectionId(@NlsContexts.Label inspectionId: String) {
      label.text = inspectionId
    }

    suspend fun setDescription(info: String) {
      readAction { myDescription.readHTMLWithCodeHighlighting(info, null) }
    }
  }

  private suspend fun expandFirstNodesWithoutChildren() {
    // If ui tree is not visible, the deferred of `promiseVisit` is cancelled (which results into cancellation of current coroutine),
    // so we need to wrap it to separate coroutine
    coroutineScope {
      launch {
        TreeUtil.promiseExpand(tree, QodanaUiTreeFirstWithMultipleChildrenVisitor()).asDeferred().await()
      }
    }
  }

  private suspend fun processTreeUpdateEvent(treeUpdate: QodanaProblemsViewModel.UiTreeEvent.Update) {
    // If ui tree is not visible, the deferred of `promiseVisit` is cancelled (which results into cancellation of current coroutine),
    // so we need to wrap it to separate coroutine
    coroutineScope {
      launch {
        val qodanaTreePath = treeUpdate.parentNodePath

        val treePath = TreeUtil.promiseVisit(tree, QodanaUiTreeVisitor(qodanaTreePath)).asDeferred().await() ?: return@launch
        val lastPathUiNode = TreeUtil.getLastUserObject(QodanaUiTreeNode::class.java, treePath) ?: return@launch
        val pathAllUiNodes = generateSequence(lastPathUiNode) { it.asViewNode().getParent(QodanaUiTreeNode::class.java) }

        treeModel.invoker.invoke {
          pathAllUiNodes.forEach {
            val node = it.asViewNode()
            if (node.update()) treeModel.nodeChanged(node.getPath())
          }
          treeStructureChanged(treePath)
        }
      }
    }
  }

  private suspend fun processTreeNavigateEvent(treeNavigate: QodanaProblemsViewModel.UiTreeEvent.Navigate) {
    // If ui tree is not visible, the deferred of `promiseVisit` is cancelled (which results into cancellation of current coroutine),
    // so we need to wrap it to separate coroutine
    coroutineScope {
      launch {
        val qodanaTreePath = treeNavigate.nodePath
        TreeUtil.promiseSelect(tree, QodanaUiTreeVisitor(qodanaTreePath)).asDeferred().await()
      }
    }
    QodanaProblemsViewTab.show(project)
    withContext(QodanaDispatchers.Ui) {
      writeIntentReadAction {
        treeNavigate.navigatable.navigate(true)
      }
    }
  }

  override fun createComparator(): Comparator<Node> = QodanaUiTreeNodeComparator

  private fun treeStructureChanged(path: TreePath? = null) {
    root.structureChanged(path)
  }

  override fun getToolbarActionGroupId(): String = "Qodana.ProblemsView"

  private suspend fun updateStatusText(statusText: StatusText, noProblemsContent: QodanaProblemsViewModel.NoProblemsContent) {
    withContext(QodanaDispatchers.Ui) {
      statusText.clear()
      statusText.appendLine(noProblemsContent.title)
      val description = noProblemsContent.description
      if (description != null) {
        statusText.appendLine(description, SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES, null)
      }
      val (firstAction, secondAction) = noProblemsContent.actions ?: return@withContext
      statusText.appendLine(firstAction.text, SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES) {
        val inputEvent = it?.source as? InputEvent
        firstAction.action.invoke(this@QodanaProblemsViewPanel, inputEvent)
      }
      if (secondAction != null) {
        statusText.appendText(" ").appendText(QodanaBundle.message("problems.toolwindow.qodana.panel.or")).appendText(" ")
        statusText.appendText(secondAction.text, SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES) {
          val inputEvent = it?.source as? InputEvent
          secondAction.action.invoke(this@QodanaProblemsViewPanel, inputEvent)
        }
      }
    }
  }

  override fun hasNextOccurence(): Boolean = problemsNavigator.hasNextOccurence()

  override fun hasPreviousOccurence(): Boolean = problemsNavigator.hasPreviousOccurence()

  override fun goNextOccurence(): OccurenceNavigator.OccurenceInfo = problemsNavigator.goNextOccurence()

  override fun goPreviousOccurence(): OccurenceNavigator.OccurenceInfo = problemsNavigator.goPreviousOccurence()

  override fun getNextOccurenceActionName(): String = problemsNavigator.nextOccurenceActionName

  override fun getPreviousOccurenceActionName(): String = problemsNavigator.previousOccurenceActionName
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun Flow<QodanaProblemsViewModel.UiTreeEvent.Update>.conflateUpdatesOfPathsWithSamePrefix(
  delayMillis: Long = 250
): Flow<QodanaProblemsViewModel.UiTreeEvent.Update> {
  var lastNotEmittedPath: QodanaTreePath? = null
  return this
    .map { it.parentNodePath.primaryDataPaths.map { path -> QodanaTreePath(path) }.asFlow() }
    .flattenConcat()
    .mapLatest { currentTreePath ->
      val needToEmitCurrentPathLater = lastNotEmittedPath == null || lastNotEmittedPath?.startsWith(currentTreePath) == true
      val currentStartsWithLastNotEmittedPath = lastNotEmittedPath?.let { currentTreePath.startsWith(it) } == true

      val pathsToEmit = when {
        needToEmitCurrentPathLater -> {
          lastNotEmittedPath = currentTreePath
          delay(delayMillis)
          listOfNotNull(lastNotEmittedPath)
        }
        currentStartsWithLastNotEmittedPath -> {
          delay(delayMillis)
          listOfNotNull(lastNotEmittedPath)
        }
        else -> {
          listOfNotNull(lastNotEmittedPath, currentTreePath)
        }
      }
      lastNotEmittedPath = null
      pathsToEmit
    }
    .map { paths ->
      paths.map { QodanaProblemsViewModel.UiTreeEvent.Update(it) }.asFlow()
    }
    .flattenConcat()
}