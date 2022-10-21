package com.jetbrains.lang.makefile.toolWindow

import com.intellij.icons.*
import com.intellij.openapi.actionSystem.*
import com.intellij.ui.*
import com.jetbrains.lang.makefile.MakefileLangBundle
import javax.swing.*

class MakefileToolWindowAutoscrollToSourceAction(
    private val options: MakefileToolWindowOptions,
    private val autoScrollHandler: AutoScrollToSourceHandler,
    private val tree: JComponent)
  : ToggleAction(MakefileLangBundle.message("action.autoscroll.to.source.text"), null, AllIcons.General.AutoscrollToSource) {

  override fun getActionUpdateThread(): ActionUpdateThread =
    ActionUpdateThread.BGT

  override fun isSelected(e: AnActionEvent): Boolean = options.autoScrollToSource

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    options.autoScrollToSource = state
    if (state) {
      autoScrollHandler.onMouseClicked(tree)
    }
  }
}