/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.dialogs

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import training.learn.LearnBundle.message
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class LearnProjectWarningDialog(project: Project?) : DialogWrapper(project, true) {
  override fun createActions(): Array<Action> {
    return arrayOf(okAction, cancelAction)
  }

  override fun createNorthPanel(): JComponent? {
    val panel = JPanel(GridBagLayout())
    val gbc = GridBagConstraints()
    val warningMessage = message("dialog.learnProjectWarning.message", ApplicationNamesInfo.getInstance().fullProductName)
    gbc.insets = Insets(4, 8, 4, 8)
    gbc.weighty = 1.0
    gbc.weightx = 1.0
    gbc.gridx = 0
    gbc.gridy = 0
    gbc.gridwidth = 2
    gbc.fill = GridBagConstraints.BOTH
    gbc.anchor = GridBagConstraints.WEST
    panel.add(JLabel(warningMessage), gbc)
    return panel
  }

  override fun createCenterPanel(): JComponent? {
    return null
  }

  init {
    title = message("dialog.learnProjectWarning.title")
    init()
  }
}