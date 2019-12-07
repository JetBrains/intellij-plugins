package name.kropp.intellij.makefile

import com.intellij.execution.configurations.*
import com.intellij.openapi.project.*
import name.kropp.intellij.makefile.psi.*
import java.io.*

class MakefileRunConfigurationFactory(runConfigurationType: MakefileRunConfigurationType) : ConfigurationFactory(runConfigurationType) {
  override fun createTemplateConfiguration(project: Project) = MakefileRunConfiguration(project, this, "name")

  fun createConfigurationFromTarget(target: MakefileTarget): MakefileRunConfiguration? {
    val configuration = MakefileRunConfiguration(target.project, this, target.name ?: "")
    val file = target.containingFile as? MakefileFile ?: return null
    configuration.filename = file.virtualFile?.path ?: ""
    configuration.target = target.name ?: ""

    if (!configuration.target.isEmpty()) {
      configuration.name = configuration.target
    } else {
      configuration.name = File(configuration.filename).name
    }

    return configuration
  }
}