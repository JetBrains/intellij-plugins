package name.kropp.intellij.makefile

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project

class MakefileRunConfigurationFactory(runConfigurationType: MakefileRunConfigurationType) : ConfigurationFactory(runConfigurationType) {
  override fun createTemplateConfiguration(project: Project) = MakefileRunConfiguration(project, this, "name")
  override fun isConfigurationSingletonByDefault() = true
}