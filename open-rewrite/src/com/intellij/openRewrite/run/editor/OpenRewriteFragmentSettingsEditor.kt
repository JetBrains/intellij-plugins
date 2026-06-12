package com.intellij.openRewrite.run.editor

import com.intellij.diagnostic.logging.LogsGroupFragment
import com.intellij.execution.ExecutionBundle
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.ui.BeforeRunComponent
import com.intellij.execution.ui.BeforeRunFragment
import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.RunConfigurationFragmentedEditor
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openRewrite.OpenRewriteBundle.message
import com.intellij.openRewrite.RECIPE_FILE_NAME
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.openRewrite.recipe.OpenRewriteType
import com.intellij.openRewrite.run.OpenRewriteRunConfiguration
import com.intellij.openRewrite.run.OpenRewriteRunConfigurationExtensionManager
import com.intellij.openRewrite.run.before.OpenRewriteInstallBeforeRunTask
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.externalSystem.service.execution.configuration.addWorkingDirectoryFragment
import com.intellij.openapi.externalSystem.service.execution.configuration.fragments.SettingsEditorFragmentContainer
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.Conditions
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findFile
import com.intellij.ui.EditorTextField
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.components.textFieldWithBrowseButton
import com.intellij.util.SmartList
import com.intellij.util.containers.addIfNotNull
import org.jetbrains.yaml.YAMLFileType.YML
import java.awt.BorderLayout
import java.util.function.Supplier
import kotlin.io.path.Path

internal class OpenRewriteFragmentSettingsEditor(runConfiguration: OpenRewriteRunConfiguration) :
  RunConfigurationFragmentedEditor<OpenRewriteRunConfiguration>(runConfiguration,
                                                                OpenRewriteRunConfigurationExtensionManager.getInstance()) {

  init {
    // Ensure recipes are loaded for active recipe completion.
    OpenRewriteRecipeService.getInstance(project).reload(ModalityState.current())
  }

  override fun createRunFragments(): List<SettingsEditorFragment<OpenRewriteRunConfiguration, *>> =
    SettingsEditorFragmentContainer.fragments {
      val beforeRunComponent = BeforeRunComponent(this@OpenRewriteFragmentSettingsEditor)
      add(BeforeRunFragment.createBeforeRun(beforeRunComponent, null))
      addAll(BeforeRunFragment.createGroup())
      add(LogsGroupFragment())

      val workingDirectoryFragment = addWorkingDirectoryFragment(
        project,
        OpenRewriteWorkingDirectoryInfo(project),
        { workingDirectory ?: "" },
        { workingDirectory = it }
      )
      workingDirectoryFragment.isRemovable = false

      val workingDirectoryField = workingDirectoryFragment.component().component
      MacrosDialog.addMacroSupport(workingDirectoryField, MacrosDialog.Filters.DIRECTORY_PATH) { false }
      val configLocationFragment = createConfigLocation()
      val configLocationField = configLocationFragment.component().component
      val configSupplier = Supplier<Collection<VirtualFile>> {
        val result = SmartList<VirtualFile>()
        val configLocation = configLocationField.text
        val workingDirectory = workingDirectoryField.getWorkingDirectoryVirtualFile()
        val configFile = when {
          configLocation.isNotBlank() -> VfsUtil.findFile(Path(configLocation), false)
          workingDirectory != null -> workingDirectory.findFile(RECIPE_FILE_NAME)
          else -> return@Supplier result
        }
        result.addIfNotNull(configFile)
        result.addAll(
          beforeRunComponent.enabledTasks.filterIsInstance<OpenRewriteInstallBeforeRunTask>().mapNotNull { it.getScratchVirtualFile() }
        )
        result
      }

      add(createActiveRecipes(configSupplier))
      add(createActiveStyles(configSupplier))
      add(configLocationFragment)
      add(createExclusions())
      add(createPlainTextMasks())
      add(createVersion())
      add(createVmOptions())
      add(createEnvironmentVariables())

      add(SettingsEditorFragment.createTag(
        "open.rewrite.dry.run",
        OpenRewriteBundle.message("open.rewrite.run.configuration.dry.run"),
        OpenRewriteBundle.OPEN_REWRITE,
        { it.dryRun },
        { configuration, value -> configuration.dryRun = value }))
    }

  private fun createActiveRecipes(configSupplier: Supplier<Collection<VirtualFile>>):
    SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<OpenRewriteActiveRecipesEditor>> {

    val field = OpenRewriteActiveRecipesEditor(project, configSupplier, OpenRewriteType.RECIPE,
                                               OpenRewriteBundle.message("open.rewrite.run.configuration.active.recipes"))
    val component = LabeledComponent.create(field, OpenRewriteBundle.message("open.rewrite.run.configuration.active.recipes"),
                                            BorderLayout.WEST)
    val fragment =
      SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<OpenRewriteActiveRecipesEditor>>(
        "open.rewrite.active.recipes",
        OpenRewriteBundle.message("open.rewrite.run.configuration.active.recipes"),
        OpenRewriteBundle.OPEN_REWRITE,
        component,
        { settings, c -> c.component.textField.setText(settings.activeRecipes) },
        { settings, c ->
          if (!c.isVisible) {
            settings.activeRecipes = null
          }
          else {
            settings.activeRecipes = c.component.textField.text
          }
        },
        Conditions.alwaysTrue())
    fragment.setHint(OpenRewriteBundle.message("open.rewrite.run.configuration.active.recipes.tooltip"))
    fragment.isRemovable = false
    fragment.addValidation {
      if (it.activeRecipes.isNullOrBlank()) {
        throw RuntimeConfigurationError(OpenRewriteBundle.message("open.rewrite.run.configuration.no.active.recipe"))
      }
    }
    return fragment
  }

  private fun createActiveStyles(configSupplier: Supplier<Collection<VirtualFile>>):
    SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<OpenRewriteActiveRecipesEditor>> {

    val field = OpenRewriteActiveRecipesEditor(project, configSupplier, OpenRewriteType.STYLE,
                                               OpenRewriteBundle.message("open.rewrite.run.configuration.active.styles"))
    val component = LabeledComponent.create(field, OpenRewriteBundle.message("open.rewrite.run.configuration.active.styles"),
                                            BorderLayout.WEST)
    val fragment =
      SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<OpenRewriteActiveRecipesEditor>>(
        "open.rewrite.active.styles",
        OpenRewriteBundle.message("open.rewrite.run.configuration.active.styles"),
        OpenRewriteBundle.OPEN_REWRITE,
        component,
        { settings, c -> c.component.textField.setText(settings.activeStyles) },
        { settings, c ->
          if (!c.isVisible) {
            settings.activeStyles = null
          }
          else {
            settings.activeStyles = c.component.textField.text
          }
        },
        { !it.activeStyles.isNullOrEmpty() })
    fragment.setHint(OpenRewriteBundle.message("open.rewrite.run.configuration.active.styles.tooltip"))
    return fragment
  }

  private fun createConfigLocation(): SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>> {
    val field = textFieldWithBrowseButton(project, FileChooserDescriptorFactory.createSingleFileDescriptor(YML)
      .withTitle(message("open.rewrite.run.configuration.config.location.browse.title")))
    val textField = field.textField as? ExtendableTextField
    if (textField != null) {
      MacrosDialog.addMacroSupport(textField, MacrosDialog.Filters.FILE_PATH) { false }
    }
    val component = LabeledComponent.create(field, OpenRewriteBundle.message("open.rewrite.run.configuration.config.location"),
                                            BorderLayout.WEST)
    return SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>>(
      "open.rewrite.config.location",
      OpenRewriteBundle.message("open.rewrite.run.configuration.config.location"),
      OpenRewriteBundle.OPEN_REWRITE,
      component,
      { settings, c -> c.component.setText(settings.configLocation) },
      { settings, c ->
        if (!c.isVisible) {
          settings.configLocation = null
        }
        else {
          settings.configLocation = c.component.text
        }
      },
      { !it.configLocation.isNullOrEmpty() })
  }

  private fun createExclusions(): SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<EditorTextField>> {
    val field = EditorTextField(mySettings.project, PlainTextFileType.INSTANCE)
    val component = LabeledComponent.create(field, OpenRewriteBundle.message("open.rewrite.run.configuration.exclusions"),
                                            BorderLayout.WEST)
    return SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<EditorTextField>>(
      "open.rewrite.exclusions",
      OpenRewriteBundle.message("open.rewrite.run.configuration.exclusions"),
      OpenRewriteBundle.OPEN_REWRITE,
      component,
      { settings, c -> c.component.setText(settings.exclusions) },
      { settings, c ->
        if (!c.isVisible) {
          settings.exclusions = null
        }
        else {
          settings.exclusions = c.component.text
        }
      },
      { !it.exclusions.isNullOrEmpty() })
  }

  private fun createPlainTextMasks(): SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<EditorTextField>> {
    val field = EditorTextField(mySettings.project, PlainTextFileType.INSTANCE)
    val component = LabeledComponent.create(field, OpenRewriteBundle.message("open.rewrite.run.configuration.plain.text.masks"),
                                            BorderLayout.WEST)
    return SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<EditorTextField>>(
      "open.rewrite.plain.text.masks",
      OpenRewriteBundle.message("open.rewrite.run.configuration.plain.text.masks"),
      OpenRewriteBundle.OPEN_REWRITE,
      component,
      { settings, c -> c.component.setText(settings.plainTextMasks) },
      { settings, c ->
        if (!c.isVisible) {
          settings.plainTextMasks = null
        }
        else {
          settings.plainTextMasks = c.component.text
        }
      },
      { !it.plainTextMasks.isNullOrEmpty() })
  }

  private fun createVersion(): SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<EditorTextField>> {
    val field = EditorTextField(mySettings.project, PlainTextFileType.INSTANCE)
    val component = LabeledComponent.create(field, OpenRewriteBundle.message("open.rewrite.run.configuration.library.version"),
                                            BorderLayout.WEST)
    return SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<EditorTextField>>(
      "open.rewrite.library.version",
      OpenRewriteBundle.message("open.rewrite.run.configuration.library.version"),
      OpenRewriteBundle.OPEN_REWRITE,
      component,
      { settings, c -> c.component.setText(settings.libraryVersion) },
      { settings, c ->
        if (!c.isVisible) {
          settings.libraryVersion = null
        }
        else {
          settings.libraryVersion = c.component.text
        }
      },
      { !it.libraryVersion.isNullOrEmpty() })
  }

  private fun createVmOptions(): SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<RawCommandLineEditor>> {
    val field = RawCommandLineEditor()
    MacrosDialog.addMacroSupport(field.editorField, MacrosDialog.Filters.ALL) { false }
    val component = LabeledComponent.create(field, ExecutionBundle.message("run.configuration.java.vm.parameters.label"),
                                            BorderLayout.WEST)
    val fragment = SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<RawCommandLineEditor>>(
      "open.rewrite.vmOptions",
      ExecutionBundle.message("run.configuration.java.vm.parameters.name"),
      OpenRewriteBundle.OPEN_REWRITE,
      component,
      { settings, c -> c.component.text = settings.vmOptions },
      { settings, c ->
        if (!c.isVisible) {
          settings.vmOptions = null
        }
        else {
          settings.vmOptions = c.component.text
        }
      },
      { !it.vmOptions.isNullOrEmpty() })
    fragment.setHint(ExecutionBundle.message("run.configuration.java.vm.parameters.hint"))
    fragment.actionHint = ExecutionBundle.message("specify.vm.options.for.running.the.application")
    return fragment
  }

  private fun createEnvironmentVariables(): SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>> {
    val envComponent = EnvironmentVariablesComponent()
    envComponent.labelLocation = BorderLayout.WEST
    CommonParameterFragments.setMonospaced(envComponent.component.textField)
    val fragment = SettingsEditorFragment<OpenRewriteRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>>(
      "open.rewrite.environmentVariables",
      ExecutionBundle.message("environment.variables.fragment.name"),
      OpenRewriteBundle.OPEN_REWRITE,
      envComponent,
      { settings, _ ->
        envComponent.envs = settings.envs
        envComponent.isPassParentEnvs = settings.passParentEnv
      },
      { settings, c ->
        if (!c.isVisible) {
          settings.envs = LinkedHashMap()
          settings.passParentEnv = true
        }
        else {
          settings.envs = LinkedHashMap(envComponent.envs)
          settings.passParentEnv = envComponent.isPassParentEnvs
        }
      },
      { it.envs.isNotEmpty() || !it.passParentEnv })
    fragment.setHint(ExecutionBundle.message("environment.variables.fragment.hint"))
    fragment.actionHint = ExecutionBundle.message("set.custom.environment.variables.for.the.process")
    return fragment
  }
}
