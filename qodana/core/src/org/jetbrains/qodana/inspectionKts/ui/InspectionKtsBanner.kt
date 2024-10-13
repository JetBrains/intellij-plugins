@file:OptIn(ExperimentalCoroutinesApi::class)

package org.jetbrains.qodana.inspectionKts.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.HelpTooltip
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.observable.util.whenDisposed
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderEx
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.util.coroutines.childScope
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.JBColor
import com.intellij.ui.border.CustomLineBorder
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.popup.list.ListPopupImpl
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.JBUI.Borders
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.annotations.Nls
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.inspectionKts.INSPECTIONS_KTS_EXTENSION
import org.jetbrains.qodana.inspectionKts.KtsInspectionsManager
import org.jetbrains.qodana.inspectionKts.isInspectionKtsEnabled
import java.awt.BorderLayout
import java.awt.Dimension
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.function.Function
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.io.path.Path

private val INSPECTION_KTS_BANNER_KEY = Key.create<Lazy<InspectionKtsBanner>>("InspectionKtsBanner")

internal class InspectionKtsBannerProvider : EditorNotificationProvider, DumbAware {
  override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
    if (!file.name.endsWith(INSPECTIONS_KTS_EXTENSION)) {
      return null
    }
    if (!isInspectionKtsEnabled()) {
      return null
    }
    return Function { fileEditor ->
      if (fileEditor !is UserDataHolderEx) return@Function null
      val filePath = try {
        file.toNioPath()
      }
      catch (_ : UnsupportedOperationException) {
        try {
          Path(file.path)
        }
        catch (_ : InvalidPathException) {
          return@Function null
        }
      }
      val newBanner = lazy { InspectionKtsBanner(filePath, project, fileEditor) }
      val currentBanner = fileEditor.putUserDataIfAbsent(INSPECTION_KTS_BANNER_KEY, newBanner)
      currentBanner.value
    }
  }
}

internal class InspectionKtsBanner(
  file: Path,
  private val project: Project,
  private val fileEditor: FileEditor
) : EditorNotificationPanel(fileEditor, inspectionKtsBannerBackgroundColor(), null) {
  init {
    val disposable = Disposer.newDisposable("InspectionKtsBanner")

    val isDisposableRegistered = Disposer.tryRegister(fileEditor, disposable)
    if (!isDisposableRegistered) {
      Disposer.dispose(disposable)
    } else {
      val scope = project.qodanaProjectScope.childScope("InspectionKtsBanner")
      disposable.whenDisposed {
        scope.cancel()
      }

      val viewModel = InspectionKtsBannerViewModelImpl(file, project, scope, KtsInspectionsManager.getInstance(project))
      init(disposable, scope, viewModel)
    }
  }

  private fun init(disposable: Disposable, scope: CoroutineScope, viewModel: InspectionKtsBannerViewModel) {
    removeAll()

    val westComponent = Wrapper().apply {
      minimumSize = Dimension(0, 0)
    }
    add(westComponent, BorderLayout.WEST)

    val recompileShortcut = CustomShortcutSet.fromString("shift alt ENTER")
    val recompileAction = RecompileAction(viewModel).apply {
      registerCustomShortcutSet(recompileShortcut, fileEditor.preferredFocusedComponent ?: fileEditor.component, disposable)
      registerCustomShortcutSet(recompileShortcut, this@InspectionKtsBanner, disposable)
    }

    val compilationToolbar = actionsToolbarWithText(
      "InspectionKtsCompilationToolbar",
      listOf(recompileAction, CompilationStatusAction(viewModel)),
      textLabelPreferredSize = Dimension(258, 34)
    )
    compilationToolbar.toolbar.targetComponent = this

    val executionErrorToolbar = actionsToolbarWithText(
      "InspectionKtsExecutionErrorToolbar",
      // hack to have a separator
      listOf(InvisibleAction(), Separator.create(), ExecutionErrorAction(viewModel))
    )
    executionErrorToolbar.mainComponent.isVisible = false
    executionErrorToolbar.toolbar.targetComponent = this
    executionErrorToolbar.mainComponent.border = Borders.emptyRight(10)

    westComponent.add(compilationToolbar.mainComponent, BorderLayout.WEST)
    westComponent.add(executionErrorToolbar.mainComponent, BorderLayout.CENTER)

    val eastComponent = Wrapper().apply {
      minimumSize = Dimension(0, 0)
    }
    add(eastComponent, BorderLayout.EAST)

    val psiViewerComponent = openPsiViewerComponent(viewModel)
    if (psiViewerComponent != null) {
      eastComponent.add(psiViewerComponent, BorderLayout.CENTER)
    }
    val examplesButton = examplesButton(project, viewModel)
    eastComponent.add(examplesButton, BorderLayout.EAST)

    scope.launch(QodanaDispatchers.Ui) {
      launch {
        viewModel.compilationStatus
          .filterNotNull()
          .collect { status ->
            compilationToolbar.toolbar.updateActionsAsync()
            compilationToolbar.textLabel.text = compilationStatusText(status)
            executionErrorToolbar.mainComponent.isVisible = status is InspectionKtsBannerViewModel.CompilationStatus.Compiled
          }
      }
      launch {
        viewModel.compilationStatus
          .filterNotNull()
          .flatMapLatest { (it as? InspectionKtsBannerViewModel.CompilationStatus.Compiled)?.executionErrorDuringAnalysis ?: flowOf(null) }
          .collect {
            executionErrorToolbar.toolbar.updateActionsAsync()
            executionErrorToolbar.textLabel.text = executionErrorText(it)
          }
      }
    }

    putClientProperty(FileEditorManager.SEPARATOR_DISABLED, true)

    border = CustomLineBorder(JBUI.CurrentTheme.Editor.BORDER_COLOR, 0, 0, 1, 0)
  }
}

@Nls
private fun compilationStatusText(compilationStatus: InspectionKtsBannerViewModel.CompilationStatus): String {
  return when(compilationStatus) {
    is InspectionKtsBannerViewModel.CompilationStatus.Cancelled -> {
      QodanaBundle.message("inspectionkts.compilation.cancelled")
    }
    is InspectionKtsBannerViewModel.CompilationStatus.Compiling -> {
      QodanaBundle.message("inspectionkts.compiling")
    }
    is InspectionKtsBannerViewModel.CompilationStatus.Compiled -> {
      if (compilationStatus.isOutdated) {
        QodanaBundle.message("inspectionkts.compiled.no.match")
      }
      else {
        QodanaBundle.message("inspectionkts.compiled")
      }
    }
    is InspectionKtsBannerViewModel.CompilationStatus.Failed -> {
      if (compilationStatus.isOutdated) {
        QodanaBundle.message("inspectionkts.compilation.failed.no.match")
      } else {
        QodanaBundle.message("inspectionkts.compilation.failed")
      }
    }
  }
}

@Nls
private fun executionErrorText(executionError: InspectionKtsBannerViewModel.ExecutionError?): String {
  return if (executionError != null) {
    QodanaBundle.message("inspectionkts.analysis.error")
  } else {
    QodanaBundle.message("inspectionkts.analysis.no.error")
  }
}

private fun actionsToolbarWithText(
  toolBarId: String,
  actions: List<AnAction>,
  textLabelPreferredSize: Dimension? = null
): ToolBarWithActionsAndText {
  val group = DefaultActionGroup()
  group.addAll(actions)
  val toolbar = ActionManager.getInstance().createActionToolbar(toolBarId, group, true)

  val label = object : JLabel() {
    override fun getPreferredSize(): Dimension {
      return textLabelPreferredSize ?: super.getPreferredSize()
    }
  }
  label.font = JBUI.Fonts.toolbarSmallComboBoxFont()
  val mainComponent = Wrapper().apply {
    add(toolbar.component, BorderLayout.WEST)
    add(label, BorderLayout.CENTER)
    isOpaque = true
    background = inspectionKtsBannerBackgroundColor()
  }
  toolbar.component.background = inspectionKtsBannerBackgroundColor()
  return ToolBarWithActionsAndText(mainComponent, toolbar, label)
}

private class ToolBarWithActionsAndText(
  val mainComponent: JPanel,
  val toolbar: ActionToolbar,
  val textLabel: JLabel
)

private fun inspectionKtsBannerBackgroundColor(): JBColor {
  return JBColor.namedColor("Editor.SearchField.background", JBColor.background())
}

private class InvisibleAction : DumbAwareAction(), CustomComponentAction {

  override fun actionPerformed(e: AnActionEvent) {
  }

  override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
    return NonOpaquePanel()
  }
}

private fun openPsiViewerComponent(viewModel: InspectionKtsBannerViewModel): Wrapper? {
  val actionLink = openPsiViewerLink(viewModel) ?: return null
  val tooltip = psiViewerTooltip()
  return Wrapper().apply {
    add(actionLink, BorderLayout.CENTER)
    add(tooltip, BorderLayout.EAST)
  }
}

private fun openPsiViewerLink(viewModel: InspectionKtsBannerViewModel): ActionLink? {
  val psiViewerOpener = viewModel.psiViewerOpener ?: return null
  return ActionLink(QodanaBundle.message("inspectionkts.open.psi.viewer")) {
    psiViewerOpener.openAction.invoke()
  }
}

private fun psiViewerTooltip(): JBLabel {
  val tooltipIcon = JBLabel(AllIcons.General.ContextHelp).apply {
    border = JBUI.Borders.empty(0, 6, 0, 12)
  }
  HelpTooltip()
    .setDescription(QodanaBundle.message("inspectionkts.open.psi.viewer.tooltip.description"))
    .setLink(
      QodanaBundle.message("inspectionkts.open.psi.viewer.tooltip.link"),
      { BrowserUtil.browse("https://plugins.jetbrains.com/docs/intellij/psi-files.html") },
      true
    )
    .setNeverHideOnTimeout(true)
    .installOn(tooltipIcon)
  return tooltipIcon
}

private fun examplesButton(project: Project, viewModel: InspectionKtsBannerViewModel): Wrapper {
  val mainComponent = Wrapper()
  val button = object : ActionButtonWithText(
    showPopupAnAction(project, viewModel, mainComponent),
    null,
    "InspectionKtsBanner",
    ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE
  ) {
    override fun shallPaintDownArrow(): Boolean {
      return true
    }
  }
  val panel = panel {
    row {
      cell(button)
    }
  }.apply {
    background = inspectionKtsBannerBackgroundColor()
  }
  mainComponent.setContent(panel)
  return mainComponent
}

private fun showPopupAnAction(project: Project, viewModel: InspectionKtsBannerViewModel, mainComponent: JPanel): DumbAwareAction {
  return object : DumbAwareAction(QodanaBundle.message("inspectionkts.banner.examples")) {
    val title = QodanaBundle.message("inspectionkts.banner.examples.title")
    override fun actionPerformed(e: AnActionEvent) {
      val popupStep = object : BaseListPopupStep<InspectionKtsBannerViewModel.Example>(title, viewModel.examples) {
        override fun onChosen(selectedValue: InspectionKtsBannerViewModel.Example, finalChoice: Boolean): PopupStep<*>? {
          return doFinalStep {
            selectedValue.openExampleAction.invoke()
          }
        }

        override fun getTextFor(value: InspectionKtsBannerViewModel.Example): String {
          return value.text
        }

        override fun getIconFor(value: InspectionKtsBannerViewModel.Example): Icon {
          return value.icon
        }
      }
      val popup = ListPopupImpl(project, popupStep)
      popup.showUnderneathOf(mainComponent)
    }
  }
}