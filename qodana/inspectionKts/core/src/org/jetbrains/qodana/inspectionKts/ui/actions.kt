@file:Suppress("ComponentNotRegistered")

package org.jetbrains.qodana.inspectionKts.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.AnimatedIcon
import org.jetbrains.qodana.inspectionKts.icons.InspectionKtsIcons
import org.jetbrains.qodana.inspectionKts.InspectionKtsBundle
import javax.swing.Icon

internal class RecompileAction(
  private val viewModel: InspectionKtsBannerViewModel,
): AnAction(InspectionKtsBundle.message("inspectionKts.action.compile.inspection.file.text"), "", AllIcons.Actions.Refresh), DumbAware {

  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val compilationStatus = viewModel.compilationStatus.value
    e.presentation.isEnabledAndVisible = compilationStatus != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    viewModel.compilationStatus.value?.recompileAction?.invoke()
  }
}

internal class CompilationStatusAction(
  private val viewModel: InspectionKtsBannerViewModel,
) : AnAction(), DumbAware {

  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val compilationStatus = viewModel.compilationStatus.value
    if (compilationStatus == null) {
      e.presentation.isEnabledAndVisible = false
      return
    }
    val icon = getActionIcon(compilationStatus)
    e.presentation.text = ""
    e.presentation.icon = getActionIcon(compilationStatus)
    e.presentation.disabledIcon = icon
    e.presentation.isEnabled = false
    if (compilationStatus is InspectionKtsBannerViewModel.CompilationStatus.Failed) {
      e.presentation.isEnabled = true
      e.presentation.text = InspectionKtsBundle.message("inspectionKts.open.log")
    }
  }

  override fun actionPerformed(e: AnActionEvent) {
    val failed = viewModel.compilationStatus.value as? InspectionKtsBannerViewModel.CompilationStatus.Failed ?: return
    failed.openExceptionInLogAction.invoke()
  }

  private fun getActionIcon(compilationStatus: InspectionKtsBannerViewModel.CompilationStatus): Icon {
    return when (compilationStatus) {
      is InspectionKtsBannerViewModel.CompilationStatus.Cancelled -> {
        AllIcons.Actions.Cancel
      }
      is InspectionKtsBannerViewModel.CompilationStatus.Compiling -> {
        AnimatedIcon.Default.INSTANCE
      }
      is InspectionKtsBannerViewModel.CompilationStatus.Failed -> {
        if (compilationStatus.isOutdated) InspectionKtsIcons.ErrorOutdated else InspectionKtsIcons.Error
      }
      is InspectionKtsBannerViewModel.CompilationStatus.Compiled -> {
        if (compilationStatus.isOutdated) InspectionKtsIcons.OkOutdated else InspectionKtsIcons.OK
      }
    }
  }
}

internal class ExecutionErrorAction(
  private val viewModel: InspectionKtsBannerViewModel,
): ActionGroup(null, "", AllIcons.General.Warning), DumbAware {

  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val compiled = viewModel.compilationStatus.value as? InspectionKtsBannerViewModel.CompilationStatus.Compiled
    val executionError = compiled?.executionErrorDuringAnalysis?.value
    e.presentation.putClientProperty(ActionUtil.SUPPRESS_SUBMENU, true)
    e.presentation.putClientProperty(ActionUtil.HIDE_DROPDOWN_ICON, true)
    e.presentation.isPopupGroup = true
    e.presentation.isPerformGroup = false
    if (executionError == null) {
      e.presentation.icon = AllIcons.General.ShowWarning
    } else {
      e.presentation.icon = AllIcons.General.Warning
    }
  }

  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    val compiled = viewModel.compilationStatus.value as? InspectionKtsBannerViewModel.CompilationStatus.Compiled
    val executionError = compiled?.executionErrorDuringAnalysis?.value ?: return emptyArray()

    val openLogAction = object : AnAction(InspectionKtsBundle.message("inspectionKts.open.log")), DumbAware {
      override fun actionPerformed(e: AnActionEvent) {
        executionError.openExceptionInLogAction.invoke()
      }
    }
    val ignoreAction = object : AnAction(InspectionKtsBundle.message("inspectionKts.ignore.error")), DumbAware {
      override fun actionPerformed(e: AnActionEvent) {
        executionError.ignoreExceptionAction.invoke()
      }
    }
    return arrayOf(openLogAction, ignoreAction)
  }
}