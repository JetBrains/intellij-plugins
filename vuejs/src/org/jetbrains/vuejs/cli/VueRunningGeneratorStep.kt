// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.cli

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.wizard.AbstractWizard
import com.intellij.ide.wizard.StepWithSubSteps
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import icons.VuejsIcons
import java.awt.BorderLayout
import java.nio.file.Paths
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants

class VueRunningGeneratorStep(private val wizard: AbstractWizard<*>,
                              private val moduleBuilder: VueCreateProjectModuleBuilder) : ModuleWizardStep(), StepWithSubSteps {
  private var controller: VueCliRunningGeneratorController? = null
  private val mainPanel = JPanel(BorderLayout())
  private val scrollPane = JBScrollPane(JPanel(),
                                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  init {
    scrollPane.border = null
    mainPanel.add(scrollPane, BorderLayout.CENTER)
  }

  override fun updateDataModel() {
    controller?.onNext()
  }

  override fun isLast() = controller?.isFinished() ?: false

  override fun isFirst() = true

  override fun doPreviousAction() {
    if (controller != null) Disposer.dispose(controller!!)
  }

  fun startGeneration(moduleContentRoot: String, settings: NpmPackageProjectGenerator.Settings) {
    wizard.updateButtons(false, false, true)
    var controller: VueCliRunningGeneratorController? = null
    var lastError: String? = null
    val listener = object : VueRunningGeneratorListener {
      override fun enableNext() {
        wizard.updateButtons(false, true, true)
      }

      override fun disableNext(validationError: String?) {
        if (controller == null) {
          lastError = validationError
        } else {
          error(validationError)
        }
      }

      override fun error(validationError: String?) {
        wizard.updateButtons(false, false, true)
        if (validationError != null && validationError.isNotBlank()) {
          Messages.showErrorDialog(wizard.currentStepComponent, validationError)
        }
      }

      override fun cancelCloseUI() {
        wizard.close(DialogWrapper.OK_EXIT_CODE)
      }

      override fun finishedQuestionsCloseUI(callback: (Project) -> Unit) {
        moduleBuilder.registerProjectCreatedCallback(callback)
        wizard.updateButtons(true, true, true)
        wizard.close(DialogWrapper.OK_EXIT_CODE)
      }
    }

    // folder must be created by generator
    FileUtil.delete(Paths.get(moduleContentRoot).toFile())
    controller = createVueRunningGeneratorController(moduleContentRoot, settings, listener, wizard.disposable)
    if (controller == null) {
      scrollPane.setViewportView(JBLabel(lastError ?: ""))
    } else {
      scrollPane.setViewportView(controller.getPanel())
      this.controller = controller
    }
  }

  @Suppress("FunctionName")
  override fun _init() {
  }

  @Suppress("FunctionName")
  override fun _commit(finishChosen: Boolean) {
  }

  override fun getIcon() = VuejsIcons.Vue!!

  override fun getComponent(): JComponent {
    return mainPanel
  }

  override fun getPreferredFocusedComponent(): JComponent {
    return mainPanel
  }
}