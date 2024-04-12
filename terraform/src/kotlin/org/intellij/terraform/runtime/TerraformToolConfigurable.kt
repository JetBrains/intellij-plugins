// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.emptyText
import com.intellij.openapi.ui.validation.validationErrorIf
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.dsl.builder.*
import kotlinx.coroutines.ensureActive
import org.intellij.terraform.config.util.TFExecutor
import org.intellij.terraform.config.util.executeSuspendable
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.installTerraform
import java.io.File
import kotlin.coroutines.coroutineContext

private const val CONFIGURABLE_ID: String = "reference.settings.dialog.project.terraform"

class TerraformToolConfigurable(private val project: Project) : BoundConfigurable(
  HCLBundle.message("terraform.name"), null
), SearchableConfigurable {

  private val configuration = TerraformProjectSettings.getInstance(project)

  private val testTerraformButton = TerraformTestButtonComponent(
    "terraform",
    {
      resultHandler -> installTerraform(project, resultHandler, EmptyProgressIndicator())
    },
    {
      val versionLine = getVersionOfTerraform(project).lineSequence().firstOrNull()?.trim()
      versionLine?.split(" ")?.getOrNull(1) ?: HCLBundle.message("terraform.executor.unrecognized.version")
    }
  )

  private lateinit var executorPathField: Cell<TextFieldWithBrowseButton>

  private suspend fun getVersionOfTerraform(project: Project): @NlsSafe String {
    val capturingProcessAdapter = CapturingProcessAdapter()

    val success = TFExecutor.`in`(project)
      .withPresentableName(HCLBundle.message("terraform.executor.version"))
      .withParameters("version")
      .withPassParentEnvironment(true)
      .withProcessListener(capturingProcessAdapter)
      .executeSuspendable()

    coroutineContext.ensureActive()

    val stdout = capturingProcessAdapter.output.stdout
    if (!success || stdout.isEmpty()) {
      throw RuntimeException("Couldn't get version of Terraform")
    }

    return stdout
  }

  override fun getId(): String = CONFIGURABLE_ID

  override fun createPanel(): DialogPanel {
    val fileChooserDescriptor = FileChooserDescriptor(true, false, false, false, false, false)

    return panel {
      row(HCLBundle.message("terraform.settings.executable.path.label")) {
        executorPathField = textFieldWithBrowseButton(
          browseDialogTitle = "",
          fileChooserDescriptor = fileChooserDescriptor,
          fileChosen = { chosenFile ->
            return@textFieldWithBrowseButton chosenFile.path
          }
        ).bindText(configuration::terraformPath).applyToComponent {
          emptyText.text = TerraformProjectSettings.getDefaultTerraformPath()
        }.onChanged {
          configuration.terraformPath = it.text
        }.trimmedTextValidation(
          validationErrorIf<String>(HCLBundle.message("terraform.invalid.path")) {
            it.isNotBlank() && !File(it).exists()
          }
        ).align(AlignX.FILL)
      }
      row {
        cell(testTerraformButton)
      }
    }
  }
}