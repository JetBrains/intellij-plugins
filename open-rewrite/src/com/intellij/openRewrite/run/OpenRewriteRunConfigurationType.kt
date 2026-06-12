package com.intellij.openRewrite.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.runConfigurationType
import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openRewrite.OpenRewriteIcons
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import javax.swing.Icon

internal const val OPEN_REWRITE_RUN_CONFIGURATION_TYPE_ID: String = "OpenRewriteRunConfigurationType"

internal fun openRewriteRunConfigurationType(): OpenRewriteRunConfigurationType = runConfigurationType<OpenRewriteRunConfigurationType>()

internal class OpenRewriteRunConfigurationType : ConfigurationType, DumbAware {
  private val factory = object : ConfigurationFactory(this) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
      return OpenRewriteRunConfiguration("", project, this)
    }

    override fun isApplicable(project: Project): Boolean =
      OpenRewriteExternalSystemBridge.EP_NAME.findFirstSafe { it.isAvailable(project) } != null

    override fun getId(): String = "OpenRewrite"
    override fun isEditableInDumbMode(): Boolean = true
    override fun getOptionsClass(): Class<out BaseState> = OpenRewriteRunConfigurationOptions::class.java
  }

  override fun getDisplayName(): String = OpenRewriteBundle.OPEN_REWRITE
  override fun getConfigurationTypeDescription(): String = OpenRewriteBundle.message("open.rewrite.recipe")
  override fun getIcon(): Icon = OpenRewriteIcons.OpenRewrite
  override fun getId(): String = OPEN_REWRITE_RUN_CONFIGURATION_TYPE_ID
  override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(factory)
}