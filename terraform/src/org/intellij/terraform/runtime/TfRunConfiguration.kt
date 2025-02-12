// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.install.TfToolType
import javax.swing.Icon

internal class TfRunConfiguration(
  project: Project,
  factory: ConfigurationFactory,
  name: String?,
  override var envFilePaths: List<String>,
) : TfToolsRunConfigurationBase(project, factory, name, envFilePaths, TfToolType.TERRAFORM) {

  override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> {
    return TfRunConfigurationEditor(this, TfToolType.TERRAFORM)
  }

  override fun getIcon(): Icon? {
    return TerraformIcons.Terraform
  }
}
