package com.jetbrains.lang.makefile.toolWindow

import com.intellij.execution.*
import com.intellij.execution.actions.*
import com.intellij.execution.impl.*
import com.intellij.execution.runners.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.ui.treeStructure.*
import com.jetbrains.lang.makefile.*

class MakefileToolWindowRunTargetAction(private val tree: Tree, private val project: Project, private val runManager: RunManagerImpl)
  : AnAction(MakefileLangBundle.message("action.run.target.tool.window.text"),
             MakefileLangBundle.message("action.run.target.tool.window.description"),
             MakefileTargetIcon) {
  override fun actionPerformed(event: AnActionEvent) {
    val selectedNodes = tree.getSelectedNodes(MakefileTargetNode::class.java, {true})
    if (selectedNodes.any()) {
      val selected = selectedNodes.first()
      val elements = MakefileTargetIndex.get(selected.name, project, GlobalSearchScope.fileScope(selected.parent.psiFile))
      val target = elements.firstOrNull() ?: return

      val dataContext = SimpleDataContext.getSimpleContext(Location.DATA_KEY.name, PsiLocation(target), event.dataContext)

      val context = ConfigurationContext.getFromContext(dataContext, event.place)

      val producer = MakefileRunConfigurationFactory(MakefileRunConfigurationType.instance)
      val configuration = RunnerAndConfigurationSettingsImpl(runManager, producer.createConfigurationFromTarget(target)
          ?: return)

      (context.runManager as RunManagerEx).setTemporaryConfiguration(configuration)
      ExecutionUtil.runConfiguration(configuration, Executor.EXECUTOR_EXTENSION_NAME.extensionList.first())
    }
  }
}
