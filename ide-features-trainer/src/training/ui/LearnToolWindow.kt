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


class LearnToolWindow internal constructor() : SimpleToolWindowPanel(true, true), DataProvider, Disposable {

  //TODO: remove public modificator set ScrollPane before release
  var scrollPane: JBScrollPane? = null
  private var myLearnPanel: LearnPanel? = null
  private var modulesPanel: ModulesPanel? = null
  private var myProject: Project? = null

  fun init(project: Project) {

    myProject = project
    reinitViewsInternal()
    scrollPane = if (LangManager.getInstance().isLangUndefined()) {
      val myLanguageChoosePanel = LanguageChoosePanel()
      JBScrollPane(myLanguageChoosePanel)
    } else {
      JBScrollPane(modulesPanel)
    }
    setContent(scrollPane!!)
  }

  fun changeLanguage() {
    reinitViewsInternal()
    val myLanguageChoosePanel = LanguageChoosePanel()
    scrollPane = JBScrollPane(myLanguageChoosePanel)
    setContent(scrollPane!!)
  }

  private fun reinitViewsInternal() {
    myLearnPanel = LearnPanel()
    modulesPanel = ModulesPanel(this)
    UiManager.modulesPanelPerProject[myProject!!] = modulesPanel!!
    UiManager.learnPanelPerProject[myProject!!] = myLearnPanel!!
  }

  //do not call on modulesPanel view view or learnPanel view
  fun reinitViews() {
    reinitViewsInternal()
  }

  override fun dispose() {
    UiManager.learnPanelPerProject.remove(myProject)
    UiManager.modulesPanelPerProject.remove(myProject)
    myLearnPanel = null
  }


}

