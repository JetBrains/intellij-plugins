// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu.runtime

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.runtime.TfCommand
import org.intellij.terraform.runtime.TfToolConfigurationTypeBase
import org.intellij.terraform.runtime.TfToolsRunConfigurationBase
import javax.swing.Icon

internal class OpenTofuConfigurationType : TfToolConfigurationTypeBase() {

  private val TF_RUN_CONFIGURATION_ID = "OpenTofuConfigurationType"

  override val baseFactory: ConfigurationFactory = createFactory(TfToolType.OPENTOFU, TfCommand.CUSTOM)
  override val initFactory: ConfigurationFactory = createFactory(TfToolType.OPENTOFU, TfCommand.INIT)
  override val validateFactory: ConfigurationFactory = createFactory(TfToolType.OPENTOFU, TfCommand.VALIDATE)
  override val planFactory: ConfigurationFactory = createFactory(TfToolType.OPENTOFU, TfCommand.PLAN)
  override val applyFactory: ConfigurationFactory = createFactory(TfToolType.OPENTOFU, TfCommand.APPLY)
  override val destroyFactory: ConfigurationFactory = createFactory(TfToolType.OPENTOFU, TfCommand.DESTROY)

  override val actionGroupId: String = "OpenTofuRunConfigurationActions"

  override fun getDisplayName(): String = HCLBundle.message("opentofu.name")
  override fun getConfigurationTypeDescription(): String = HCLBundle.message("terraform.configuration.type.description", TfToolType.OPENTOFU.displayName)
  override fun getIcon(): Icon = TerraformIcons.Opentofu
  override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(
    baseFactory, initFactory, validateFactory, planFactory, applyFactory, destroyFactory
  )

  override fun getId(): String = TF_RUN_CONFIGURATION_ID

  override fun ConfigurationFactory.createConfiguration(project: Project): TfToolsRunConfigurationBase {
    return OpenTofuRunConfiguration(project, this, "", emptyList())
  }

}