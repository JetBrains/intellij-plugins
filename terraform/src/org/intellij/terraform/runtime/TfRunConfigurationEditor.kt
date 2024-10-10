// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.diagnostic.logging.LogsGroupFragment
import com.intellij.execution.ui.*
import com.intellij.execution.ui.utils.fragments
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.util.Computable
import org.intellij.terraform.hcl.HCLBundle
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import javax.swing.JComponent

internal class TfRunConfigurationEditor(runConfiguration: TerraformRunConfiguration) :
  RunConfigurationFragmentedEditor<TerraformRunConfiguration>(runConfiguration) {

  private val commandComboBox = ComboBox(TerraformFileConfigurationProducer.Type.entries.toTypedArray())
    .withLabelToTheLeft(HCLBundle.message("terraform.run.configuration.command.label"))

  override fun createRunFragments(): MutableList<SettingsEditorFragment<TerraformRunConfiguration, *>> =
    fragments<TerraformRunConfiguration>(HCLBundle.message("terraform.run.text"), "terraform.run.configuration") {
      fragment("terraform.command", commandComboBox) {
        apply = { model, ui ->
          model.programParameters = ui.component.selectedItem?.toString()
        }
        reset = { model, ui ->
          ui.component.selectedItem = model.programParameters
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