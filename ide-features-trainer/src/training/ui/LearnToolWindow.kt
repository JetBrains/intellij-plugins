/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBScrollPane
import training.lang.LangManager
import training.ui.views.LanguageChoosePanel
import training.ui.views.LearnPanel
import training.ui.views.ModulesPanel

class LearnToolWindow internal constructor(private val project: Project) : SimpleToolWindowPanel(true, true), DataProvider, Disposable {

  //TODO: remove public modificator set ScrollPane before release
  var scrollPane: JBScrollPane
    private set
  private lateinit var myLearnPanel: LearnPanel
  private lateinit var modulesPanel: ModulesPanel

  init {
    reinitViewsInternal()
    scrollPane = if (LangManager.getInstance().isLangUndefined()) {
      JBScrollPane(LanguageChoosePanel())
    } else {
      JBScrollPane(modulesPanel)
    }
    setContent(scrollPane)
  }

  fun changeLanguage() {
    reinitViewsInternal()
    scrollPane = JBScrollPane(LanguageChoosePanel())
    setContent(scrollPane)
  }

  private fun reinitViewsInternal() {
    myLearnPanel = LearnPanel()
    modulesPanel = ModulesPanel(this)
    UiManager.modulesPanelPerProject[project] = modulesPanel
    UiManager.learnPanelPerProject[project] = myLearnPanel
  }

  //do not call on modulesPanel view view or learnPanel view
  fun reinitViews() {
    reinitViewsInternal()
  }

  override fun dispose() {
    UiManager.learnPanelPerProject.remove(project)
    UiManager.modulesPanelPerProject.remove(project)
  }

}

