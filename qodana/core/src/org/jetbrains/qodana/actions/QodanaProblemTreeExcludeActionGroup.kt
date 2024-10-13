package org.jetbrains.qodana.actions

import com.intellij.ide.BrowserUtil
import com.intellij.ide.actions.SmartPopupActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeNode
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeRoot
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel
import javax.swing.JTree

class QodanaProblemTreeExcludeActionGroup : SmartPopupActionGroup(), DumbAware {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun getChildrenCountThreshold(): Int = 1

  override fun update(e: AnActionEvent) {
    val isLoadedUiState = getLoadedUiState(e) != null
    val isUiNodeSelected = getSelectedUiNode(e) != null
    e.presentation.isHideGroupIfEmpty = true
    e.presentation.isEnabledAndVisible = isUiNodeSelected && isLoadedUiState
  }

  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    e ?: return emptyArray()
    val loadedUiState = getLoadedUiState(e) ?: return emptyArray()
    val uiNode = getSelectedUiNode(e) ?: return emptyArray()

    return uiNode.getExcludeActionsDescriptors().map { QodanaExcludeNodeAction(loadedUiState, it) }.toTypedArray()
  }
}

private fun getSelectedUiNode(e: AnActionEvent): QodanaUiTreeNode<*, *>? {
  val tree = e.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT) as? JTree
  return tree?.selectionPath?.lastPathComponent as? QodanaUiTreeNode<*, *>
}

private fun getLoadedUiState(e: AnActionEvent): QodanaProblemsViewModel.UiState.Loaded? {
  return e.qodanaProblemsViewModel?.uiStateFlow?.value as? QodanaProblemsViewModel.UiState.Loaded
}

private class QodanaExcludeNodeAction(
  private val loadedUiState: QodanaProblemsViewModel.UiState.Loaded,
  private val excludeActionDescriptor: QodanaUiTreeNode.ExcludeActionDescriptor
) : DumbAwareAction(excludeActionDescriptor.actionName) {
  override fun actionPerformed(e: AnActionEvent) {
    loadedUiState.exclude(excludeActionDescriptor.configExcludeItem)
  }
}

class QodanaShowBuildPageAction : DumbAwareAction() {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val rootNode = getSelectedUiNode(e) as? QodanaUiTreeRoot
    val jobUrl = getLoadedUiState(e)?.jobUrl
    if (rootNode == null || jobUrl == null) {
      e.presentation.isEnabledAndVisible = false
      return
    }
  }
  override fun actionPerformed(e: AnActionEvent) {
    val jobUrl = getLoadedUiState(e)?.jobUrl ?: return
    BrowserUtil.browse(jobUrl)
  }
}

class QodanaOpenBrowserPageAction : DumbAwareAction() {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val rootNode = getSelectedUiNode(e) as? QodanaUiTreeRoot
    if (rootNode == null) {
      e.presentation.isEnabledAndVisible = false
      return
    }
  }
  override fun actionPerformed(e: AnActionEvent) {
    val browserViewProvider = e.qodanaProblemsViewModel?.browserViewProviderStateFlow?.value ?: return
    e.project?.qodanaProjectScope?.launch(QodanaDispatchers.Default) {
      browserViewProvider.openBrowserView()
    }
  }
}
