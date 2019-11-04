/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
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
    //check if the lesson view is active
    val project = e.project
    if (project == null) { LOG.warn("Unable to perform NextLesson action for 'null' project"); return }
    val myLearnToolWindow = LearnToolWindowFactory.learnWindowPerProject[project] ?: throw Exception("Unable to get Learn toolwindow for project (${project.name})")
    val view = myLearnToolWindow.scrollPane.viewport?.view ?: throw Exception("Unable to get Learn toolwindow scrollpane or viewport (${project.name})")
    //click button to skip or go to the next lesson
    (view as? LearnPanel)?.clickButton()
  }

  companion object {
    private val LOG = Logger.getInstance(NextLessonAction::class.java)
  }
}
