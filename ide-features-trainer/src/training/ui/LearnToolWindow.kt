// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui

import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBScrollPane
import training.lang.LangManager
import training.ui.views.LanguageChoosePanel
import training.ui.views.LearnPanel
import training.ui.views.ModulesPanel

class LearnToolWindow internal constructor(val project: Project) : SimpleToolWindowPanel(true, true), DataProvider {
  var scrollPane: JBScrollPane
    private set
  lateinit var learnPanel: LearnPanel
    private set
  lateinit var modulesPanel: ModulesPanel
    private set

  init {
    reinitViewsInternal()
    scrollPane = if (LangManager.getInstance().isLangUndefined()) {
      JBScrollPane(LanguageChoosePanel(this))
    }
    else {
      JBScrollPane(modulesPanel)
    }
    setContent(scrollPane)
  }

  private fun reinitViewsInternal() {
    learnPanel = LearnPanel()
    modulesPanel = ModulesPanel(this)
  }

  fun setLearnPanel() {
    scrollPane.setViewportView(learnPanel)
    scrollPane.revalidate()
    scrollPane.repaint()
  }

  fun setModulesPanel() {
    modulesPanel.updateMainPanel()
    scrollPane.setViewportView(modulesPanel)
    scrollPane.revalidate()
    scrollPane.repaint()
  }

  fun setChooseLanguageView() {
    scrollPane.setViewportView(LanguageChoosePanel(this))
    scrollPane.revalidate()
    scrollPane.repaint()
  }

  fun updateScrollPane() {
    scrollPane.viewport.revalidate()
    scrollPane.viewport.repaint()
    scrollPane.revalidate()
    scrollPane.repaint()
  }

  //do not call on modulesPanel view or learnPanel view
  fun reinitViews() {
    reinitViewsInternal()
  }
}

