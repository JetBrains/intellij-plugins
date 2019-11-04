/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.ui.JBUI
import training.learn.LearnBundle
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class AskToSwitchToLearnProjectBackDialog(private val learnProject: Project, currentProject: Project) : DialogWrapper(currentProject, true) {

  init {
    title = LearnBundle.message("dialog.askToSwitchToLearnProject.title")
    init()
    setButtonsAlignment(SwingConstants.CENTER)
  }

  override fun createActions(): Array<Action> = arrayOf(okAction)

  override fun createCenterPanel(): JComponent? {
    val panel = JPanel(GridBagLayout())
    val gbc = GridBagConstraints()

    val warningMessage = LearnBundle.message("dialog.askToSwitchToLearnProject.message", learnProject.name)

    gbc.insets = JBUI.insets(4, 8, 4, 8)
    gbc.weighty = 1.0
    gbc.weightx = 1.0
    gbc.gridx = 0
    gbc.gridy = 0
    gbc.gridwidth = 2
    gbc.fill = GridBagConstraints.BOTH
    gbc.anchor = GridBagConstraints.WEST
    panel.add(JLabel("<html>${warningMessage}</html>"), gbc)

    return panel
  }


}
