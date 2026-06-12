package com.intellij.openRewrite.run

import com.intellij.execution.ExecutionBundle
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.util.ProgramParametersUtil
import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openRewrite.run.editor.OpenRewriteFragmentSettingsEditor
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element

class OpenRewriteRunConfiguration(name: String, project: Project, factory: ConfigurationFactory) :
  LocatableConfigurationBase<Element>(project, factory, name) {
  override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
    OpenRewriteFragmentSettingsEditor(this)

  var workingDirectory: String?
    get() = options.workingDirectory
    set(value) {
      options.workingDirectory = value
    }

  var dryRun: Boolean
    get() = options.dryRun
    set(value) {
      options.dryRun = value
    }

  var activeRecipes: String?
    get() = options.activeRecipes
    set(value) {
      options.activeRecipes = value
    }

  var activeStyles: String?
    get() = options.activeStyles
    set(value) {
      options.activeStyles = value
    }

  var configLocation: String?
    get() = options.configLocation
    set(value) {
      options.configLocation = value
    }

  var exclusions: String?
    get() = options.exclusions
    set(value) {
      options.exclusions = value
    }

  var plainTextMasks: String?
    get() = options.plainTextMasks
    set(value) {
      options.plainTextMasks = value
    }

  var libraryVersion: String?
    get() = options.libraryVersion
    set(value) {
      options.libraryVersion = value
    }

  var vmOptions: String?
    get() = options.vmOptions
    set(value) {
      options.vmOptions = value
    }

  var envs: MutableMap<String, String>
    get() = options.envs
    set(value) {
      options.envs = value
    }

  var passParentEnv: Boolean
    get() = options.passParentEnv
    set(value) {
      options.passParentEnv = value
    }

  override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
    val delegate = OpenRewriteExternalSystemBridge.findDelegate(this)
                   ?: throw ExecutionException(OpenRewriteBundle.message("open.rewrite.run.configuration.no.delegate"))
    return delegate.getState(executor, environment)
  }

  override fun getOptionsClass(): Class<out RunConfigurationOptions> = OpenRewriteRunConfigurationOptions::class.java

  override fun getOptions(): OpenRewriteRunConfigurationOptions = super.getOptions() as OpenRewriteRunConfigurationOptions

  override fun suggestedName(): String? {
    val recipe = activeRecipes?.substringBefore(',') ?: return null
    return recipe.substringAfterLast('.')
  }

  @Throws(RuntimeConfigurationException::class)
  override fun checkConfiguration() {
    if (options.workingDirectory.isNullOrBlank()) {
      throw RuntimeConfigurationException(ExecutionBundle.message("run.configuration.working.directory.empty.error"))
    }
    if (options.activeRecipes.isNullOrBlank()) {
      throw RuntimeConfigurationException(OpenRewriteBundle.message("open.rewrite.run.configuration.no.active.recipe"))
    }
    if (OpenRewriteExternalSystemBridge.findDelegate(this) == null) {
      throw RuntimeConfigurationException(OpenRewriteBundle.message("open.rewrite.run.configuration.no.delegate"))
    }
  }

  fun getExpandedWorkingDirectory(): String? = ProgramParametersUtil.expandPathAndMacros(workingDirectory, null, project)

  fun getExpandedConfigLocation(): String? = ProgramParametersUtil.expandPathAndMacros(configLocation, null, project)
}