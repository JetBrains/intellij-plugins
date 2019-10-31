/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.dialogs

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.StateRestoringCheckBox
import training.learn.LearnBundle.message
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class SdkModuleProblemDialog(private val project: Project) : DialogWrapper(project, true) {
  private var myCbOpenProjectSdkPreferences: StateRestoringCheckBox? = null

  override fun createActions(): Array<Action> {
    return arrayOf(okAction, cancelAction)
  }

  override fun createNorthPanel(): JComponent? {
    val panel = JPanel(GridBagLayout())
    val gbc = GridBagConstraints()
    val warningMessage = message("dialog.emptyModule.message")
    gbc.insets = Insets(4, 8, 4, 8)
    gbc.weighty = 1.0
    gbc.weightx = 1.0
    gbc.gridx = 0
    gbc.gridy = 0
    gbc.gridwidth = 2
    gbc.fill = GridBagConstraints.BOTH
    gbc.anchor = GridBagConstraints.WEST
    panel.add(JLabel(warningMessage), gbc)
    gbc.gridy++
    gbc.gridx = 0
    gbc.weightx = 0.0
    gbc.gridwidth = 1
    myCbOpenProjectSdkPreferences = StateRestoringCheckBox()
    myCbOpenProjectSdkPreferences!!.text = message("dialog.emptyModule.checkbox")
    panel.add(myCbOpenProjectSdkPreferences, gbc)
    myCbOpenProjectSdkPreferences!!.isSelected = true
    return panel
  }

  override fun createCenterPanel(): JComponent? {
    return null
  }

  override fun doOKAction() {
    if (DumbService.isDumb(project)) {
      Messages.showMessageDialog(project, "Changing Project SDK is not available while indexing is in progress", "Indexing", null)
      return
    }
    if (myCbOpenProjectSdkPreferences != null && myCbOpenProjectSdkPreferences!!.isSelected) {
      ModulesConfigurator.showDialog(project, null, null)
      super.doOKAction()
    }
  }

  init {
    title = message("dialog.emptyModule.title")
    init()
  }
}