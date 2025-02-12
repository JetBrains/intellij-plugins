// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
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
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.dsl.builder.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.FailedInstallation
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.install.getToolVersion
import org.intellij.terraform.install.installTFTool
import org.intellij.terraform.opentofu.runtime.OpenTofuProjectSettings
import kotlin.io.path.Path

private const val CONFIGURABLE_ID: String = "reference.settings.dialog.project.terraform"
private const val PARSE_DELAY = 100L
private val VERSION_REGEX = Regex("^v*(\\d+)(\\.\\d+)*(\\.\\d+)*-?\\S*$")

internal class TfToolConfigurable(private val project: Project) : BoundConfigurable(
  HCLBundle.message("terraform.opentofu.settings.label"), null
), SearchableConfigurable {

  private val terraformConfig = TfProjectSettings.getInstance(project)
  private val openTofuConfig = OpenTofuProjectSettings.getInstance(project)

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
      fileChooserDescriptor = FileChooserDescriptor(true, false, false, false, false, false),
      fileChosen = { chosenFile ->
        return@textFieldWithBrowseButton chosenFile.path
      }
    ).bindText(settings::toolPath).applyToComponent {
      emptyText.text = type.getBinaryName()
    }.trimmedTextValidation(
      validationErrorIf(HCLBundle.message("tool.executor.invalid.path")) { filePath ->
        !ToolPathDetector.getInstance(project).isExecutable(Path(filePath))
      }
    ).align(AlignX.FILL)
    val testButton = testToolButton(type, project.service<ToolPathDetector>(), parentDisposable, executorField.component)
    row {
      cell(testButton)
    }
    executorField.onChanged {
      testButton.updateTestButton(it.text)
    }
  }

  private fun testToolButton(
    type: TfToolType,
    pathDetector: ToolPathDetector,
    parentDisposable: Disposable?,
    executorPathField: TextFieldWithBrowseButton?,
  ) = ToolExecutableTestButtonComponent(
    type,
    parentDisposable,
    executorPathField,
    { resultHandler ->
      try {
        installTFTool(project, resultHandler, EmptyProgressIndicator(), type)
      }
      catch (ex: Exception) {
        fileLogger().warnWithDebug("Failed to install ${type.displayName}", ex)
        resultHandler(FailedInstallation { ex.message ?: HCLBundle.message("tool.executor.unknown.error") })
      }
    }
  ) {
    val executorPathText = withContext(Dispatchers.EDT) { executorPathField?.text }
    if (executorPathText.isNullOrEmpty() || !pathDetector.isExecutable(Path(executorPathText))) { //Trying to detect tool in PATH variable
      val detectedPath = pathDetector.detect(executorPathText.orEmpty().ifBlank { type.executableName })
      if (!detectedPath.isNullOrEmpty()) {
        withContext(Dispatchers.EDT) { executorPathField?.text = detectedPath }
      }
    }
    val updatedPathText = withContext(Dispatchers.EDT) { executorPathField?.text }
    if (!updatedPathText.isNullOrEmpty() && pathDetector.isExecutable(Path(updatedPathText))) {
      val versionLine = getToolVersion(project, type, updatedPathText).lineSequence().firstOrNull()?.trim()
      val version = versionLine?.split(" ")?.firstOrNull {
        VERSION_REGEX.matches(StringUtil.newBombedCharSequence(it, PARSE_DELAY))
      } ?: throw IllegalStateException(HCLBundle.message("tool.executor.unrecognized.version", type.executableName))
      return@ToolExecutableTestButtonComponent version
    }
    return@ToolExecutableTestButtonComponent ""
  }
}
