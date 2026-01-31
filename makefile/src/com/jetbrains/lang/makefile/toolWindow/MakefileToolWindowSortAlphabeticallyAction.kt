package com.jetbrains.lang.makefile.toolWindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.jetbrains.lang.makefile.MakefileLangBundle
import javax.swing.tree.DefaultTreeModel

class MakefileToolWindowSortAlphabeticallyAction(private val options: MakefileToolWindowOptions, private val model: DefaultTreeModel) :
    ToggleAction(MakefileLangBundle.message("action.sort.alphabetically.text"), null, AllIcons.ObjectBrowser.Sorted) {
  override fun getActionUpdateThread(): ActionUpdateThread =
    ActionUpdateThread.BGT

  override fun isSelected(e: AnActionEvent): Boolean = options.sortAlphabetically

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    options.sortAlphabetically = state
    model.setRoot(options.getRootNode())
  }
}