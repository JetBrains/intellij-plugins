package name.kropp.intellij.makefile

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import javax.swing.JPanel

class MakefileRunConfigurationType : ConfigurationType {
  override fun getDisplayName() = "Makefile"
  override fun getIcon() = MakefileIcon
  override fun getConfigurationTypeDescription() = "Makefile Target"

  override fun getId() = "MAKEFILE_TARGET_RUN_CONFIGURATION"

  override fun getConfigurationFactories() = arrayOf(MakefileRunConfigurationFactory(this))
}

class MakefileRunConfigurationFactory(runConfigurationType: MakefileRunConfigurationType) : ConfigurationFactory(runConfigurationType) {
  override fun createTemplateConfiguration(project: Project): RunConfiguration {
    return MakefileRunConfiguration(project, this, "name")
  }
}

class MakefileRunConfiguration(project: Project, factory: MakefileRunConfigurationFactory, name: String) : RunConfigurationBase(project, factory, name) {
  override fun checkConfiguration() {
  }

  override fun getConfigurationEditor() = MakefileRunConfigurationEditor()

  override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState? {
    return null
  }
}

class MakefileRunConfigurationEditor : SettingsEditor<MakefileRunConfiguration>() {
  private val panel: JPanel

  init {
    panel = JPanel()
    panel.add(TextFieldWithBrowseButton())
  }

  override fun createEditor() = panel

  override fun applyEditorTo(p0: MakefileRunConfiguration) {
  }

  override fun resetEditorFrom(p0: MakefileRunConfiguration) {
  }
}
