// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.runConfigurationType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.hcl.HCLBundle
import org.jetbrains.annotations.Nls
import javax.swing.Icon

internal class TfConfigurationType : ConfigurationType, DumbAware {
  val baseFactory: ConfigurationFactory = createFactory(TfMainCommand.CUSTOM)
  val initFactory: ConfigurationFactory = createFactory(TfMainCommand.INIT)
  val validateFactory: ConfigurationFactory = createFactory(TfMainCommand.VALIDATE)
  val planFactory: ConfigurationFactory = createFactory(TfMainCommand.PLAN)
  val applyFactory: ConfigurationFactory = createFactory(TfMainCommand.APPLY)
  val destroyFactory: ConfigurationFactory = createFactory(TfMainCommand.DESTROY)

  override fun getDisplayName(): String = HCLBundle.message("terraform.name")
  override fun getConfigurationTypeDescription(): String = HCLBundle.message("terraform.configuration.type.description")
  override fun getIcon(): Icon = TerraformIcons.Terraform
  override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(
    baseFactory, initFactory, validateFactory, planFactory, applyFactory, destroyFactory
  )

  override fun getId(): String = TF_RUN_CONFIGURATION_ID

  private fun createFactory(type: TfMainCommand): ConfigurationFactory = object : ConfigurationFactory(this) {
    override fun getName(): @Nls String {
      val name = super.getName()
      return "$name ${type.title}".trim()
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
      val configuration = TerraformRunConfiguration(project, this, "", emptyList())
      val path = project.basePath
      if (path != null) {
        configuration.workingDirectory = path
      }
      configuration.commandType = type
      if (type == TfMainCommand.CUSTOM) {
        configuration.programArguments = "-help"
      }
      return configuration
    }

    // The same like getName(), but not localized
    override fun getId(): String = type.id

    override fun isApplicable(project: Project): Boolean = true
  }
}

private const val TF_RUN_CONFIGURATION_ID = "TerraformConfigurationType"

internal fun tfRunConfigurationType(): TfConfigurationType = runConfigurationType<TfConfigurationType>()