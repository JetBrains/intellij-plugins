package name.kropp.intellij.makefile

import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.RunManagerEx
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import name.kropp.intellij.makefile.psi.MakefileTarget

class MakefileRunTargetAction(private val target: MakefileTarget) : AnAction("make ${target.name}", "make ${target.name}", MakefileTargetIcon) {
  override fun actionPerformed(event: AnActionEvent) {
    val dataContext = SimpleDataContext.getSimpleContext(Location.DATA_KEY.name, PsiLocation(target), event.dataContext)

    val context = ConfigurationContext.getFromContext(dataContext)

    val producer = MakefileRunConfigurationProducer()
    val configuration = producer.findOrCreateConfigurationFromContext(context)?.configurationSettings ?: return

    (context.runManager as RunManagerEx).setTemporaryConfiguration(configuration)
    ExecutionUtil.runConfiguration(configuration, ExecutorRegistry.getInstance().registeredExecutors.first())
  }
}