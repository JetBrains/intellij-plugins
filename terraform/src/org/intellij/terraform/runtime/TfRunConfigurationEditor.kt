// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.diagnostic.logging.LogsGroupFragment
import com.intellij.execution.ui.*
import com.intellij.execution.ui.utils.fragments
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.util.Computable
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.SimpleListCellRenderer
import org.intellij.terraform.hcl.HCLBundle
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JComponent

internal class TfRunConfigurationEditor(runConfiguration: TerraformRunConfiguration) :
  RunConfigurationFragmentedEditor<TerraformRunConfiguration>(runConfiguration) {

  private val commandComboBox = ComboBox(TfMainCommand.entries.toTypedArray()).apply {
    selectedItem = TfMainCommand.NONE
    renderer = SimpleListCellRenderer.create { label, value, _ ->
      if (value != TfMainCommand.NONE) {
        label.text = value.command
      }
      else {
        label.text = HCLBundle.message("terraform.run.configuration.command.combobox.none.item")
      }
      font = Font(Font.MONOSPACED, font.style, font.size)
    }
  }.withLabelToTheLeft(HCLBundle.message("terraform.run.configuration.command.label"))

  private val programArguments = RawCommandLineEditor()
    .withLabelToTheLeft(HCLBundle.message("terraform.run.configuration.arguments.label"))

  override fun createRunFragments(): MutableList<SettingsEditorFragment<TerraformRunConfiguration, *>> =
    fragments<TerraformRunConfiguration>(HCLBundle.message("terraform.run.text"), "terraform.run.configuration") {
      fragment("terraform.command", commandComboBox) {
        apply = { model, ui ->
          model.commandType = ui.component.selectedItem as? TfMainCommand ?: TfMainCommand.NONE
        }
        reset = { model, ui ->
          ui.component.selectedItem = model.commandType
        }
        isRemovable = false
      }

      fragment("terraform.arguments", programArguments) {
        apply = { model, ui ->
          model.programArguments = ui.component.text
        }
        reset = { model, ui ->
          ui.component.text = model.programArguments
        }
        isRemovable = false
      }
    }.apply {
      add(CommonParameterFragments.createWorkingDirectory(project, Computable { null }))

      // 'Operating System'
      add(CommonTags.parallelRun())
      add(CommonParameterFragments.createEnvParameters())

      // 'Logs'
      add(LogsGroupFragment())

      // 'Before Launch'
      val beforeRunComponent = BeforeRunComponent(this@TfRunConfigurationEditor)
      add(BeforeRunFragment.createBeforeRun(beforeRunComponent, null))
      addAll(BeforeRunFragment.createGroup())
    }

  private fun <C : JComponent> C.withLabelToTheLeft(@Nls label: String): LabeledComponent<C> {
    return LabeledComponent.create(this, label).apply {
      labelLocation = BorderLayout.WEST
    }
  }
}