package name.kropp.intellij.makefile

import com.intellij.execution.configurations.ConfigurationType

class MakefileRunConfigurationType : ConfigurationType {
  override fun getDisplayName() = "Makefile"
  override fun getIcon() = MakefileIcon
  override fun getConfigurationTypeDescription() = "Makefile Target"

  override fun getId() = "MAKEFILE_TARGET_RUN_CONFIGURATION"

  override fun getConfigurationFactories() = arrayOf(MakefileRunConfigurationFactory(this))
}