package name.kropp.intellij.makefile

import com.intellij.execution.*
import com.intellij.execution.actions.*
import com.intellij.execution.runners.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.*
import name.kropp.intellij.makefile.psi.*

class MakefileRunTargetAction(private val target: MakefileTarget) : AnAction("make ${target.name}", "make ${target.name}", MakefileTargetIcon) {
  override fun actionPerformed(event: AnActionEvent) {
    val dataContext = SimpleDataContext.getSimpleContext(Location.DATA_KEY.name, PsiLocation(target), event.dataContext)

    val context = ConfigurationContext.getFromContext(dataContext)

    val producer = MakefileRunConfigurationProducer()
    val configuration = producer.findOrCreateConfigurationFromContext(context)?.configurationSettings ?: return

    (context.runManager as RunManagerEx).setTemporaryConfiguration(configuration)
    ExecutionUtil.runConfiguration(configuration, Executor.EXECUTOR_EXTENSION_NAME.extensionList.first())
  }
}