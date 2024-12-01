package com.jetbrains.lang.makefile.toolWindow

import com.intellij.execution.Executor
import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.RunManagerEx
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.treeStructure.Tree
import com.jetbrains.lang.makefile.*

@Suppress("DialogTitleCapitalization")
internal class MakefileToolWindowRunTargetAction(private val tree: Tree, private val project: Project, private val runManager: RunManagerImpl)
  : AnAction(MakefileLangBundle.message("action.run.target.tool.window.text"),
             MakefileLangBundle.message("action.run.target.tool.window.description"),
             MakefileTargetIcon) {
  override fun actionPerformed(event: AnActionEvent) {
    val selectedNodes = tree.getSelectedNodes(MakefileTargetNode::class.java) { true }
    if (selectedNodes.any()) {
      val selected = selectedNodes.first()
      if (selected.parent.psiFile == null)
        return
      val elements = MakefileTargetIndex.getInstance().getTargets(selected.name, project,
                                                                          GlobalSearchScope.fileScope(selected.parent.psiFile!!))
      val target = elements.firstOrNull() ?: return

      val dataContext = SimpleDataContext.getSimpleContext(Location.DATA_KEY, PsiLocation(target), event.dataContext)

      val context = ConfigurationContext.getFromContext(dataContext, event.place)

      val producer = MakefileRunConfigurationFactory(MakefileRunConfigurationType.instance)
      val configuration = RunnerAndConfigurationSettingsImpl(runManager, producer.createConfigurationFromTarget(target)
          ?: return)

      (context.runManager as RunManagerEx).setTemporaryConfiguration(configuration)
      ExecutionUtil.runConfiguration(configuration, Executor.EXECUTOR_EXTENSION_NAME.extensionList.first())
    }
  }
}
