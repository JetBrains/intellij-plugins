// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.diagnostic.logging.LogsGroupFragment
import com.intellij.execution.ui.*
import com.intellij.execution.ui.utils.fragments
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Computable
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.util.ui.UIUtil
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JComponent

internal class TfRunConfigurationEditor(runConfiguration: TfToolsRunConfigurationBase, private val toolType: TfToolType) :
  RunConfigurationFragmentedEditor<TfToolsRunConfigurationBase>(runConfiguration) {

  private val commandComboBox = ComboBox(TfCommand.entries.toTypedArray()).apply {
    selectedItem = TfCommand.CUSTOM
    renderer = SimpleListCellRenderer.create { label, value, _ ->
      if (value != TfCommand.CUSTOM) {
        label.text = value.command
      }
      else {
        label.text = HCLBundle.message("terraform.run.configuration.command.combobox.none.item")
        label.foreground = UIUtil.getInactiveTextColor()
      }
      font = Font(Font.MONOSPACED, font.style, font.size)
    }
  }.withLabelToTheLeft(HCLBundle.message("terraform.run.configuration.command.label"))

  private val globalOptions = RawCommandLineEditor().withLabelToTheLeft(HCLBundle.message("terraform.run.configuration.global.options.label"))

  private val programArguments = RawCommandLineEditor().withLabelToTheLeft(HCLBundle.message("terraform.run.configuration.arguments.label"))

  override fun createRunFragments(): MutableList<SettingsEditorFragment<TfToolsRunConfigurationBase, *>> =
    fragments<TfToolsRunConfigurationBase>(HCLBundle.message("terraform.run.text", toolType.getBinaryName()), "terraform.run.configuration") {
      fragment("terraform.global.options", globalOptions) {
        name = HCLBundle.message("terraform.run.configuration.global.options.fragment")
        apply = { model, ui ->
          model.passGlobalOptions = ui.isVisible
          model.globalOptions = ui.component.text
        }
        reset = { model, ui ->
          ui.component.text = model.globalOptions
        }
        visible = { model -> model.passGlobalOptions }
      }

      fragment("terraform.command", commandComboBox) {
        apply = { model, ui ->
          model.commandType = ui.component.selectedItem as? TfCommand ?: TfCommand.CUSTOM
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
        validation = { model, _ ->
          if (model.commandType == TfCommand.CUSTOM && model.programArguments.isBlank()) {
            ValidationInfo(HCLBundle.message("terraform.run.configuration.arguments.empty.validation.text"))
          }
          else null
        }
        isRemovable = false
      }
    }.apply {
      add(CommonParameterFragments.createWorkingDirectory(project, Computable { null }))

      // 'Operating System'
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