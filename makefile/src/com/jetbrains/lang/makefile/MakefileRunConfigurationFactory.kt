package com.jetbrains.lang.makefile

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.project.Project
import com.jetbrains.lang.makefile.psi.MakefileTarget
import java.io.File

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