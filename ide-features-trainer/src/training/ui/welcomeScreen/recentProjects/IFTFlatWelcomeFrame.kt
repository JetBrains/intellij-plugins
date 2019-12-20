// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui.welcomeScreen.recentProjects

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.wm.WelcomeScreen
import com.intellij.openapi.wm.impl.welcomeScreen.FlatWelcomeFrame
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import javax.swing.JPanel

class IFTFlatWelcomeFrame : FlatWelcomeFrame() {

  private val originalWelcomeScreen: JPanel
    get() = UIUtil.findComponentsOfType(component, JPanel::class.java).first { it is WelcomeScreen }
  private val originalCenterWelcomeScreen: NonOpaquePanel
    get() = UIUtil.uiChildren(originalWelcomeScreen).filterIsInstance(NonOpaquePanel::class.java).first()
  private val originalRecentProjectsPanel: JPanel?
    get() = UIUtil.uiChildren(originalWelcomeScreen).filterNot { it == originalCenterWelcomeScreen }.firstOrNull() as JPanel?

  init {
    if (showCustomWelcomeScreen) {
      replaceRecentProjectsPanel()
    }
  }

  private fun replaceRecentProjectsPanel() {
    getRootPane().preferredSize = JBUI.size(MAX_DEFAULT_WIDTH, height)
    val groupsPanel = GroupsPanel(ApplicationManager.getApplication()).customizeActions()
    val recentProjectPanel = UIUtil.uiChildren(originalRecentProjectsPanel).first()
    if (recentProjectPanel == null) {
      originalWelcomeScreen.add(groupsPanel.wrap(), BorderLayout.WEST)
    }
    else {
      originalRecentProjectsPanel?.remove(recentProjectPanel)
      originalRecentProjectsPanel?.add(groupsPanel)
    }
    repaint()
  }

}

internal val showCustomWelcomeScreen
  get() = Registry.`is`("ideFeaturesTrainer.welcomeScreen.tutorialsTree")
