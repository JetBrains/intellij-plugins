package com.jetbrains.lang.makefile

import com.intellij.execution.*
import com.intellij.execution.actions.*
import com.intellij.execution.runners.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.*
import com.jetbrains.lang.makefile.psi.*

class MakefileRunTargetAction(private val target: MakefileTarget)
  : AnAction(MakefileLangBundle.message("action.run.target.text", target.name),
             MakefileLangBundle.message("action.run.target.description", target.name),
             MakefileTargetIcon) {
  override fun actionPerformed(event: AnActionEvent) {
    val dataContext = SimpleDataContext.getSimpleContext(Location.DATA_KEY.name, PsiLocation(target), event.dataContext)

    val context = ConfigurationContext.getFromContext(dataContext, event.place)

    val producer = MakefileRunConfigurationProducer()
    val configuration = producer.findOrCreateConfigurationFromContext(context)?.configurationSettings ?: return

    (context.runManager as RunManagerEx).setTemporaryConfiguration(configuration)
    ExecutionUtil.runConfiguration(configuration, Executor.EXECUTOR_EXTENSION_NAME.extensionList.first())
  }
}