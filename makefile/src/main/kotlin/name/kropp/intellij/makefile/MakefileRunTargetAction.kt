package name.kropp.intellij.makefile

import com.intellij.execution.Executor
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.RunConfigurationProducerService
import com.intellij.execution.RunManagerEx
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.configurations.ConfigurationUtil
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import name.kropp.intellij.makefile.psi.MakefileTarget

class MakefileRunTargetAction(private val target: MakefileTarget) : AnAction("make ${target.name}", "make ${target.name}", MakefileTargetIcon) {
  override fun actionPerformed(event: AnActionEvent) {
/*
    val factory = ConfigurationTypeUtil.findConfigurationType(MakefileRunConfigurationType::class.java).configurationFactories.first()
*/
/*
    val configuration = factory.createTemplateConfiguration(event.project!!)
    configuration.filename = target.containingFile.virtualFile.path
    configuration.target = target.name!!
*//*



    val context = ConfigurationContext.getFromContext(event.dataContext)

    val producer = MakefileRunConfigurationProducer()
    val configuration = producer.findExistingConfiguration(context)!!

    (context.runManager as RunManagerEx).setTemporaryConfiguration(configuration)
    ExecutionUtil.runConfiguration(configuration, ExecutorRegistry.getInstance().registeredExecutors.first())

//    RunConfigurationProducerService.getInstance(event.project!!)



    val configurations = context.configurationsFromContext?.filter {
      it.configuration is MakefileRunConfiguration
    }
//    val configuration = configurations?.firstOrNull()
*/
  }
}