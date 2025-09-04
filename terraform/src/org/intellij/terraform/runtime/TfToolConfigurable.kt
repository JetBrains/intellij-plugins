// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.emptyText
import com.intellij.openapi.ui.validation.DialogValidation
import com.intellij.openapi.ui.validation.validationErrorIf
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.*
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfExecutableTestButton
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.opentofu.runtime.OpenTofuProjectSettings
import org.intellij.terraform.terragrunt.runtime.TerragruntProjectSettings
import kotlin.io.path.Path
import kotlin.io.path.exists

private const val CONFIGURABLE_ID: String = "reference.settings.dialog.project.terraform"

internal class TfToolConfigurable(private val project: Project) : BoundConfigurable(
  HCLBundle.message("terraform.opentofu.settings.label"), null
), SearchableConfigurable {

  private val terraformConfig = TfProjectSettings.getInstance(project)
  private val openTofuConfig = OpenTofuProjectSettings.getInstance(project)
  private val terragruntConfig = TerragruntProjectSettings.getInstance(project)

  override fun getId(): String = CONFIGURABLE_ID
  override fun getHelpTopic(): String = "terraform"

  override fun createPanel(): DialogPanel {
    return panel {
      group(HCLBundle.message("terraform.name")) {
        executableToolSettingsPanel(this, terraformConfig, TfToolType.TERRAFORM, disposable)
      }
      group(HCLBundle.message("opentofu.name")) {
        executableToolSettingsPanel(this, openTofuConfig, TfToolType.OPENTOFU, disposable)
      }
      group(JBLabel(HCLBundle.message("terragrunt.name")).apply {
        icon = AllIcons.General.Beta
        horizontalTextPosition = JBLabel.LEFT
      }) {
        executableToolSettingsPanel(this, terragruntConfig, TfToolType.TERRAGRUNT, disposable)
      }
    }
  }

  private fun executableToolSettingsPanel(
    parent: Panel,
    settings: TfToolSettings,
    type: TfToolType,
    parentDisposable: Disposable?,
  ) = parent.apply {
    val myRow = row(HCLBundle.message("tool.settings.executable.path.label", type.displayName)) {}
    val executorField = myRow.textFieldWithBrowseButton(
      fileChooserDescriptor = FileChooserDescriptorFactory.singleFile(),
      fileChosen = { chosenFile ->
        return@textFieldWithBrowseButton chosenFile.path
      }
    ).bindText(settings::toolPath).applyToComponent {
      emptyText.text = type.getBinaryName()
    }.trimmedTextValidation(IS_EXIST, CHECK_EXECUTABLE).align(AlignX.FILL)
    val testButton = TfExecutableTestButton(project, type, executorField.component, parentDisposable)
    row {
      cell(testButton)
    }
    executorField.onChanged {
      testButton.updateTestButton(it.text)
    }
  }
}

private val IS_EXIST: DialogValidation.WithParameter<() -> String> = validationErrorIf(HCLBundle.message("tool.executor.invalid.path")) { filePath ->
  !Path(filePath).exists()
}

private val CHECK_EXECUTABLE: DialogValidation.WithParameter<() -> String> = validationErrorIf(HCLBundle.message("tool.executor.non.executable.path")) { filePath ->
  !TfToolPathDetector.isExecutable(Path(filePath))
}