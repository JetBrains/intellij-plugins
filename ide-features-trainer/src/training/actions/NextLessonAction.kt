// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import training.ui.LearnToolWindowFactory
import training.ui.views.LearnPanel

/**
 * Skip lesson or go to the next lesson
 *
 * Shortcuts:
 * win: alt + shift + right
 * mac: ctrl + shift + right
 * (see the plugin.xml to change shortcuts)
 */
class NextLessonAction : AnAction() {

  override fun actionPerformed(e: AnActionEvent) {
    val view = getLearnPanel(e)
    if (view == null) {
      LOG.warn("No LearnPanel available")
      return
    }
    //click button to skip or go to the next lesson
    view.clickButton()
  }

  override fun update(e: AnActionEvent) {
    val presentation = e.presentation
    presentation.isEnabled = getLearnPanel(e)?.hasNextButton() ?: false
  }

  private fun getLearnPanel(e: AnActionEvent): LearnPanel? {
    val project = e.project ?: return null
    // Learning panel can be closed at all
    val myLearnToolWindow = LearnToolWindowFactory.learnWindowPerProject[project] ?: return null
    return myLearnToolWindow.scrollPane.viewport?.view as? LearnPanel
  }

  companion object {
    private val LOG = Logger.getInstance(NextLessonAction::class.java)
  }
}
