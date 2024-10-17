// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.wsl.WslPath
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.emptyText
import com.intellij.openapi.ui.validation.validationErrorIf
import com.intellij.ui.dsl.builder.*
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TFToolType
import org.intellij.terraform.install.getToolVersion
import org.intellij.terraform.install.installTFTool
import org.intellij.terraform.opentofu.runtime.OpenTofuProjectSettings
import java.io.File

private const val CONFIGURABLE_ID: String = "reference.settings.dialog.project.terraform"

class TerraformToolConfigurable(private val project: Project) : BoundConfigurable(
  HCLBundle.message("terraform.opentofu.settings.label"), null
), SearchableConfigurable {

  private val terraformConfig = TerraformProjectSettings.getInstance(project)
  private val openTofuConfig = OpenTofuProjectSettings.getInstance(project)

  override fun getId(): String = CONFIGURABLE_ID

  override fun createPanel(): DialogPanel {
    return panel {
      group(HCLBundle.message("terraform.name")) {
        executableToolSettingsPanel(this, terraformConfig, TFToolType.TERRAFORM)
      }
      group(HCLBundle.message("opentofu.name")) {
        executableToolSettingsPanel(this, openTofuConfig, TFToolType.OPENTOFU)
      }
    }
  }

  private fun executableToolSettingsPanel(
    parent: Panel,
    settings: ToolSettings,
    type: TFToolType,
  ) = parent.apply {
    val pathDetector = type.getPathDetector(project)
    val myRow = row(HCLBundle.message("tool.settings.executable.path.label", type.getBinaryName())) {}
    val executorField = myRow.textFieldWithBrowseButton(
      fileChooserDescriptor = FileChooserDescriptor(true, false, false, false, false, false),
      fileChosen = { chosenFile ->
        return@textFieldWithBrowseButton chosenFile.path
      }
    ).bindText(settings::toolPath).applyToComponent {
      emptyText.text = pathDetector.detectedPath() ?: type.getBinaryName()
    }.onChanged {
      settings.toolPath = it.text
    }.trimmedTextValidation(
      validationErrorIf(HCLBundle.message("tool.executor.invalid.path")) { filePath ->
        val wslDistribution = WslPath.getDistributionByWindowsUncPath(filePath)
        filePath.isNotBlank() && (!File(filePath).exists() || wslDistribution == null)
      }
    ).align(AlignX.FILL)
    row {
      cell(testToolButton(type, pathDetector, settings, executorField))
    }
  }

  private fun testToolButton(
    type: TFToolType,
    pathDetector: ToolPathDetector,
    toolSettings: ToolSettings,
    executorPathField: Cell<TextFieldWithBrowseButton>?,
  ) = ToolExecutableTestButtonComponent(
    pathDetector,
    type,
    HCLBundle.message("tool.testButton.text"),
    { resultHandler ->
      try {
        installTFTool(project, resultHandler, EmptyProgressIndicator(), type, toolSettings)
      }
      catch (ex: Exception) {
        fileLogger().warn("Failed to install ${type.displayName}", ex)
        resultHandler(false)
      }
    }
  ) {
    if (pathDetector.detectedPath() == null && pathDetector.detect()) {
      executorPathField?.component?.emptyText?.text = pathDetector.detectedPath() ?: type.getBinaryName()
    }
    val versionLine = getToolVersion(project, type).lineSequence().firstOrNull()?.trim()
    versionLine?.split(" ")?.getOrNull(1) ?: HCLBundle.message("tool.executor.unrecognized.version", type)
  }
}
