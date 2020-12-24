package name.kropp.intellij.makefile.toolWindow

import com.intellij.execution.*
import com.intellij.execution.actions.*
import com.intellij.execution.impl.*
import com.intellij.execution.runners.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.ui.treeStructure.*
import name.kropp.intellij.makefile.*

class MakefileToolWindowRunTargetAction(private val tree: Tree, private val project: Project, private val runManager: RunManagerImpl) : AnAction("Run target", "Run target", MakefileTargetIcon) {
  override fun actionPerformed(event: AnActionEvent) {
    val selectedNodes = tree.getSelectedNodes(MakefileTargetNode::class.java, {true})
    if (selectedNodes.any()) {
      val selected = selectedNodes.first()
      val elements = MakefileTargetIndex.get(selected.name, project, GlobalSearchScope.fileScope(selected.parent.psiFile))
      val target = elements.first()

      val dataContext = SimpleDataContext.getSimpleContext(Location.DATA_KEY.name, PsiLocation(target), event.dataContext)

      val context = ConfigurationContext.getFromContext(dataContext)

      val producer = MakefileRunConfigurationFactory(MakefileRunConfigurationType)
      val configuration = RunnerAndConfigurationSettingsImpl(runManager, producer.createConfigurationFromTarget(target)
          ?: return)

      (context.runManager as RunManagerEx).setTemporaryConfiguration(configuration)
      ExecutionUtil.runConfiguration(configuration, Executor.EXECUTOR_EXTENSION_NAME.extensionList.first())
    }
  }
}