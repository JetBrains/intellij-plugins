// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.runConfigurationType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.opentofu.runtime.OpenTofuConfigurationType
import org.jetbrains.annotations.Nls

internal abstract class TfToolConfigurationTypeBase : ConfigurationType, DumbAware {

  abstract val baseFactory: ConfigurationFactory
  abstract val initFactory: ConfigurationFactory
  abstract val validateFactory: ConfigurationFactory
  abstract val planFactory: ConfigurationFactory
  abstract val applyFactory: ConfigurationFactory
  abstract val destroyFactory: ConfigurationFactory

  abstract val actionGroupId: String

  protected fun createFactory(tfToolType: TfToolType, type: TfCommand): ConfigurationFactory = object : ConfigurationFactory(this) {

    override fun getName(): @Nls String {
      val command = type.command.ifEmpty { "custom" }
      return "${tfToolType.displayName} ${HCLBundle.message("terraform.run.configuration.$command.name.suffix")}"
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
      val configuration = createConfiguration(project)
      val path = project.basePath
      if (path != null) {
        configuration.workingDirectory = path
      }
      configuration.commandType = type
      if (type == TfCommand.CUSTOM) {
        configuration.programArguments = "-help"
      }
      return configuration
    }

    // The same like getName(), but not localized
    override fun getId(): String = "${tfToolType.displayName} ${type.command}"

    override fun isApplicable(project: Project): Boolean = true
  }

  protected abstract fun ConfigurationFactory.createConfiguration(project: Project): TfToolsRunConfigurationBase
}

internal fun tfRunConfigurationType(toolType: TfToolType): TfToolConfigurationTypeBase {
  return when (toolType) {
    TfToolType.TERRAFORM -> return runConfigurationType<TfConfigurationType>()
    TfToolType.OPENTOFU -> return runConfigurationType<OpenTofuConfigurationType>()
  }
}