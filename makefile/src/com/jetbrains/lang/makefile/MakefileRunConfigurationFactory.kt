package com.jetbrains.lang.makefile

import com.intellij.execution.configurations.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.jetbrains.lang.makefile.psi.*
import java.io.*

class MakefileRunConfigurationFactory(private val runConfigurationType: MakefileRunConfigurationType) : ConfigurationFactory(runConfigurationType) {
  override fun getId(): String = "Makefile"
  override fun getName(): String = runConfigurationType.displayName

  override fun createTemplateConfiguration(project: Project) = MakefileRunConfiguration(project, this, "name")

  fun createConfigurationFromTarget(target: MakefileTarget): MakefileRunConfiguration? {
    val configuration = MakefileRunConfiguration(target.project, this, target.name)
    val file = target.containingFile as? MakefileFile ?: return null
    val macroManager = PathMacroManager.getInstance(target.project)
    val path = file.virtualFile?.path ?: return null
    configuration.filename = macroManager.collapsePath(path) ?: ""
    configuration.target = target.name

    if (configuration.target.isNotEmpty()) {
      configuration.name = configuration.target
    } else {
      configuration.name = File(path).name
    }

    return configuration
  }
}