// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager
import training.learn.CourseManager
import training.learn.lesson.LessonManager
import training.ui.LearnToolWindowFactory

class NextNonPassedLessonAction : AnAction(AllIcons.Actions.NextOccurence) {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val lesson = LearnToolWindowFactory.learnWindowPerProject[project]?.learnPanel?.lesson ?: LessonManager.instance.currentLesson ?: return
    val nextNonPassedLesson = CourseManager.instance.getNextNonPassedLesson(lesson) ?: return
    CourseManager.instance.openLesson(project, nextNonPassedLesson)
  }

  override fun update(e: AnActionEvent) {
    val project = e.project ?: return
    val toolWindowManager = ToolWindowManager.getInstance(project)
    val toolWindow = toolWindowManager.getToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW) ?: return
    e.presentation.isEnabled = toolWindow.isVisible && CourseManager.instance.getNextNonPassedLesson(null) != null
  }
}
