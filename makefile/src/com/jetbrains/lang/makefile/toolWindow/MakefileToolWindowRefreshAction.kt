package com.jetbrains.lang.makefile.toolWindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.lang.makefile.MakefileLangBundle
import javax.swing.tree.DefaultTreeModel

@Suppress("DialogTitleCapitalization")
class MakefileToolWindowRefreshAction(private val model: DefaultTreeModel, private val options: MakefileToolWindowOptions)
  : AnAction(MakefileLangBundle.message("action.refresh.targets.text"), MakefileLangBundle.message("action.refresh.targets.description"), AllIcons.Actions.Refresh){
  override fun getActionUpdateThread(): ActionUpdateThread =
    ActionUpdateThread.BGT

  override fun actionPerformed(e: AnActionEvent) {
      model.setRoot(options.getRootNode())
  }
}