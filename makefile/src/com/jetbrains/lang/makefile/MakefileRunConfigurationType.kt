package com.jetbrains.lang.makefile

import com.intellij.execution.configurations.*
import icons.MakefileIcons

object MakefileRunConfigurationType : ConfigurationType {
  override fun getDisplayName() = MakefileLangBundle.message("run.configuration.name")
  override fun getIcon() = MakefileIcons.Makefile
  override fun getConfigurationTypeDescription() = MakefileLangBundle.message("run.configuration.description")

  override fun getId() = "MAKEFILE_TARGET_RUN_CONFIGURATION"

  override fun getConfigurationFactories() = arrayOf(MakefileRunConfigurationFactory(this))
}