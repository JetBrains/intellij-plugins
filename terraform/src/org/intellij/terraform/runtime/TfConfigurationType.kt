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
  val baseFactory: ConfigurationFactory = MyConfigurationFactory(null, null, "Terraform")
  val planFactory: ConfigurationFactory = MyConfigurationFactory(HCLBundle.message("terraform.run.configuration.type.plan.name.suffix"), "plan", "Terraform Plan")
  val applyFactory: ConfigurationFactory = MyConfigurationFactory(HCLBundle.message("terraform.run.configuration.type.apply.name.suffix"), "apply", "Terraform Apply")

  override fun getDisplayName(): String = HCLBundle.message("terraform.name")
  override fun getConfigurationTypeDescription(): String = HCLBundle.message("terraform.configuration.type.description")
  override fun getIcon(): Icon = TerraformIcons.Terraform
  override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(baseFactory)
  override fun getId(): String = TF_RUN_CONFIGURATION_ID

  private inner class MyConfigurationFactory(
    private val nameSuffix: @Nls String?,
    private val parameters: String?,
    private val id: String,
  ) : ConfigurationFactory(this) {
    override fun getName(): @Nls String {
      val name = super.getName()
      if (nameSuffix != null) return "$name $nameSuffix"
      return name
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
      val configuration = TerraformRunConfiguration(project, this, "", emptyList())
      val path = project.basePath
      if (path != null) {
        configuration.workingDirectory = path
      }
      if (parameters != null) {
        configuration.programParameters = parameters
      }
      return configuration
    }

    override fun getId(): String = id
    override fun isApplicable(project: Project): Boolean = true
  }
}

private const val TF_RUN_CONFIGURATION_ID = "TerraformConfigurationType"

internal fun tfRunConfigurationType(): TfConfigurationType = runConfigurationType<TfConfigurationType>()