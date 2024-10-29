// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu.runtime

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.runtime.TfRunConfigurationEditor
import org.intellij.terraform.runtime.TfToolsRunConfigurationBase
import javax.swing.Icon

internal class OpenTofuRunConfiguration(
  project: Project,
  factory: ConfigurationFactory,
  name: String?,
  override var envFilePaths: List<String>,
) : TfToolsRunConfigurationBase(project, factory, name, envFilePaths, TfToolType.OPENTOFU) {

  override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> {
    return TfRunConfigurationEditor(this, TfToolType.OPENTOFU)
  }

  override fun getIcon(): Icon? {
    return TerraformIcons.Opentofu
  }
}
