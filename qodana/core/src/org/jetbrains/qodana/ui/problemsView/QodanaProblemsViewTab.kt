package org.jetbrains.qodana.ui.problemsView

import com.intellij.analysis.problemsView.toolWindow.ProblemsView
import com.intellij.analysis.problemsView.toolWindow.ProblemsViewState
import com.intellij.analysis.problemsView.toolWindow.ProblemsViewTab
import com.intellij.analysis.problemsView.toolWindow.ProblemsViewToolWindowUtils
import com.intellij.codeInsight.hint.HintUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.actionSystem.UiDataProvider
import com.intellij.openapi.components.*
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.IconButton
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi
import com.intellij.platform.util.coroutines.childScope
import com.intellij.ui.*
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.panels.HorizontalLayout
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.ui.content.Content
import com.intellij.util.PlatformUtils
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import icons.QodanaIcons
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectDisposable
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModelImpl
import java.awt.Color
import java.awt.ComponentOrientation
import java.util.concurrent.atomic.AtomicReference
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class QodanaProblemsViewTab(
  private val project: Project,
  initializeViewEagerly: Boolean
) : BorderLayoutPanel(), ProblemsViewTab, Disposable, UiDataProvider {
  companion object {
    const val ID = "QODANA_PROBLEMS_VIEW_TAB"

    fun isVisible(project: Project): Boolean {
      val isToolWindowVisible = try {
        ProblemsViewToolWindowUtils.getToolWindow(project)?.isVisible == true
      }
      catch (_: IllegalStateException) {
        false
      }
      return isToolWindowVisible && ProblemsViewToolWindowUtils.getSelectedTab(project) is QodanaProblemsViewTab
    }

    fun initView(project: Project) {
      val tab = ProblemsViewToolWindowUtils.getTabById(project, ID) ?: return
      (tab as? QodanaProblemsViewTab)?.lazyInitView
    }

    suspend fun show(project: Project) {
      // in tests tabs are not created, but we rely on view's subscriptions for "Open in IDE" tests (node selection, editor navigation)
      if (application.isUnitTestMode) {
        withContext(QodanaDispatchers.Ui) {
          val toolWindowHasQodanaTab = ProblemsViewToolWindowUtils.getTabById(project, ID) != null
          if (!toolWindowHasQodanaTab) {
            QodanaProblemsViewTab(project, initializeViewEagerly = true).installOnToolWindow()
          }
        }
      }
      ProblemsViewToolWindowUtils.selectTabAsync(project, ID)
    }
  }

  private class ViewWrapper(
    val viewModel: QodanaProblemsViewModel,
    val problemsViewPanel: QodanaProblemsViewPanel
  )

  private val viewWrapper = AtomicReference<ViewWrapper?>(null)

  val viewModel: QodanaProblemsViewModel?
    get() = viewWrapper.get()?.viewModel

  private val problemsViewPanel: QodanaProblemsViewPanel?
    get() = viewWrapper.get()?.problemsViewPanel

  private val lazyInitView by lazy {
    initializeView()
  }

  private val scope = project.qodanaProjectScope.childScope()

  init {
    if (initializeViewEagerly) {
      lazyInitView
    }
    Disposer.register(project.qodanaProjectDisposable, this)
  }

  private fun initializeView() {
    val viewModel = QodanaProblemsViewModelImpl(scope, project)
    val problemsViewPanel = QodanaProblemsViewPanel(scope, viewModel, ID, project, ProblemsViewState.getInstance(project))
    Disposer.register(this, problemsViewPanel)

    val viewWrapper = ViewWrapper(viewModel, problemsViewPanel)
    this.viewWrapper.set(viewWrapper)

    addToCenter(problemsViewPanel)

    scope.launch(QodanaDispatchers.Ui) {
      viewModel.bannersContentProvidersFlow.collectLatest { bannerContentProviders ->
        val notificationPanels = bannerContentProviders.map { NotificationBannerPanel(scope, problemsViewPanel, it) }

        val aggregatePanel = JPanel().apply {
          layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }
        notificationPanels.forEach {
          aggregatePanel.add(it)
        }
        addToTop(aggregatePanel)
        revalidate()
        repaint()

        try {
          awaitCancellation()
        }
        finally {
          remove(aggregatePanel)
        }
      }
    }
  }

  private fun installOnToolWindow() {
    val contentManager = ProblemsView.getToolWindow(project)?.contentManager ?: return
    val content = contentManager.factory.createContent(this, getName(0), false)
    content.isCloseable = false
    contentManager.addContent(content)
  }

  override fun customizeTabContent(content: Content) {
    content.apply {
      putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
      putUserData(Content.TAB_LABEL_ORIENTATION_KEY, ComponentOrientation.RIGHT_TO_LEFT)
      putUserData(ToolWindowContentUi.NOT_SELECTED_TAB_ICON_TRANSPARENT, false)
      setPromoIcon(this)
    }
  }

  private fun setPromoIcon(content: Content) {
    val tabWasEverOpenedStateFlow = QodanaTabWasEverOpenedService.getInstance().wasEverOpenedStateFlow
    if (tabWasEverOpenedStateFlow.value) return

    content.icon = QodanaIcons.Icons.New
    scope.launch(QodanaDispatchers.Ui) {
      removeIconWhenTabIsOpened(tabWasEverOpenedStateFlow)
    }
  }

  private suspend fun removeIconWhenTabIsOpened(tabWasEverOpenedFlow: Flow<Boolean>) {
    tabWasEverOpenedFlow.filter { it }.first()
    delay(300) // immediate icon removal after tab opening doesn't look good
    val contentToRemoveIconFrom = ProblemsViewToolWindowUtils.getContentById(project, ID) ?: return
    contentToRemoveIconFrom.icon = null
  }

  override fun uiDataSnapshot(sink: DataSink) {
    sink[QodanaProblemsViewModel.DATA_KEY] = viewModel
    sink[QodanaProblemsViewPanel.DATA_KEY] = problemsViewPanel
    DataSink.uiDataSnapshot(sink, problemsViewPanel)
  }

  override fun getName(count: Int): String {
    return problemsViewPanel?.getName(count) ?: QodanaBundle.message("problems.toolwindow.qodana.panel.name")
  }

  override fun getTabId(): String = ID

  override fun orientationChangedTo(vertical: Boolean) {
    problemsViewPanel?.orientationChangedTo(vertical)
  }

  override fun selectionChangedTo(selected: Boolean) {
    if (selected) {
      QodanaTabWasEverOpenedService.getInstance().setWasOpened()
      lazyInitView
    }
    problemsViewPanel?.selectionChangedTo(selected)
  }

  override fun visibilityChangedTo(visible: Boolean) {
    problemsViewPanel?.visibilityChangedTo(visible)
  }

  private inner class NotificationBannerPanel(
    val scope: CoroutineScope,
    val problemsViewPanel: QodanaProblemsViewPanel,
    val bannerContentProvider: BannerContentProvider
  ) : BorderLayoutPanel() {
    init {
      border = JBUI.Borders.merge(JBUI.Borders.empty(10, 0, 10, 10), IdeBorderFactory.createBorder(JBColor.border(), SideBorder.BOTTOM), true)

      addToCenter(JBLabel().apply {
        icon = AllIcons.General.BalloonInformation
        text = bannerContentProvider.text
        iconTextGap = JBUI.scale(8)

        val leftBorder = problemsViewPanel.toolbarInsets?.left?.plus(4) ?: 0
        border = JBUI.Borders.emptyLeft(leftBorder)
      })
      addToRight(NonOpaquePanel(HorizontalLayout(16)).apply {
        bannerContentProvider.actions.forEach {
          add(createBannerActionLabel(it))
        }
        add(createCloseActionButton())
      })
    }

    override fun getBackground(): Color? {
      return EditorColorsManager.getInstance().globalScheme.getColor(HintUtil.PROMOTION_PANE_KEY) ?: super.getBackground()
    }

    private fun createBannerActionLabel(action: BannerContentProvider.Action): HyperlinkLabel {
      return HyperlinkLabel(action.text).apply {
        addHyperlinkListener {
          scope.launch(QodanaDispatchers.Default) {
            action.callback.invoke()
          }
        }
      }
    }

    private fun createCloseActionButton(): JComponent {
      return InplaceButton(IconButton("", AllIcons.Actions.Close, AllIcons.Actions.CloseHovered)) {
        bannerContentProvider.onClose.invoke()
      }
    }
  }

  override fun dispose() {
    scope.cancel()
  }
}

@Service(Service.Level.APP)
@State(name = "QodanaTabWasOpened", storages = [Storage(value = "qodana.xml")])
private class QodanaTabWasEverOpenedService : PersistentStateComponent<QodanaTabWasEverOpenedService.State> {
  companion object {
    fun getInstance(): QodanaTabWasEverOpenedService = service()
  }

  private val _wasEverOpenedStateFlow = MutableStateFlow(isNewDisabledByDefault())
  val wasEverOpenedStateFlow = _wasEverOpenedStateFlow.asStateFlow()

  fun setWasOpened(wasOpened: Boolean = true) {
    _wasEverOpenedStateFlow.value = wasOpened
  }

  override fun getState(): State {
    return State().apply {
      wasOpened = _wasEverOpenedStateFlow.value
    }
  }

  override fun loadState(state: State) {
    _wasEverOpenedStateFlow.value = state.wasOpened
  }

  class State : BaseState() {
    var wasOpened by property(isNewDisabledByDefault())
  }
}

/**
 * Intellij IDEA has new Problem' view tab, provided by Security Analysis plugin
 */
private fun isNewDisabledByDefault(): Boolean {
  return PlatformUtils.isIntelliJ()
}

class ResetQodanaTabPromoIconAction : DumbAwareAction() {
  override fun actionPerformed(e: AnActionEvent) {
    QodanaTabWasEverOpenedService.getInstance().setWasOpened(false)
  }
}
