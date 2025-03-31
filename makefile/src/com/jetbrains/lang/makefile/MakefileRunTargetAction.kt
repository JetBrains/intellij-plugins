package com.jetbrains.lang.makefile

import com.intellij.execution.Executor
import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.RunManagerEx
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.jetbrains.lang.makefile.psi.MakefileTarget

@Suppress("DialogTitleCapitalization")
internal class MakefileRunTargetAction(private val target: MakefileTarget) : AnAction() {
  init {
    templatePresentation.setText(MakefileLangBundle.message("action.run.target.text", target.name), false)
    templatePresentation.description = MakefileLangBundle.message("action.run.target.description", target.name)
    templatePresentation.icon = MakefileTargetIcon
  }

  override fun actionPerformed(event: AnActionEvent) {
    val dataContext = SimpleDataContext.getSimpleContext(Location.DATA_KEY, PsiLocation(target), event.dataContext)

    val context = ConfigurationContext.getFromContext(dataContext, event.place)

    val producer = MakefileRunConfigurationProducer()
    val configuration = producer.findOrCreateConfigurationFromContext(context)?.configurationSettings ?: return

    (context.runManager as RunManagerEx).setTemporaryConfiguration(configuration)
    ExecutionUtil.runConfiguration(configuration, Executor.EXECUTOR_EXTENSION_NAME.extensionList.first())
  }
}