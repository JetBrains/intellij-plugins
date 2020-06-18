// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.containers.ContainerUtil
import training.learn.CourseManager
import training.learn.LearnBundle
import training.ui.LearnToolWindowFactory

class LearningDocumentationModeAction : ToggleAction(LearnBundle.messagePointer("action.LearningDocumentationModeAction.text"), AllIcons.Actions.ListFiles) {
  private val selectedMap = ContainerUtil.createWeakMap<Project, Boolean>()

  fun isSelectedInProject(project: Project): Boolean = selectedMap[project] ?: false

  override fun isSelected(e: AnActionEvent): Boolean = e.project?.let { isSelectedInProject(it) } ?: false

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    val project = e.project ?: return
    selectedMap[project] = state
    val currentLesson = LearnToolWindowFactory.learnWindowPerProject[project]?.learnPanel?.lesson ?: return
    CourseManager.instance.openLesson(project, currentLesson)
  }

  override fun update(e: AnActionEvent) {
    super.update(e)
    val project = e.project ?: return
    val toolWindowManager = ToolWindowManager.getInstance(project)
    val toolWindow = toolWindowManager.getToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW) ?: return
    e.presentation.isEnabled = toolWindow.isVisible
  }
}
