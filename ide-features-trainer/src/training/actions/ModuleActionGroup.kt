// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.util.ui.EmptyIcon
import training.learn.interfaces.Module
import training.statistic.StatisticBase
import training.ui.LearnIcons
import training.ui.welcomeScreen.recentProjects.RenderableAction
import javax.swing.Icon

class ModuleActionGroup(val module: Module) : DefaultActionGroup(module.name, module.lessons.map { OpenLessonAction(it) }),
                                              RenderableAction {
  override var isValid: Boolean = true
  var isExpanded = false
  override val action: AnAction = this
  override val name: String = module.name
  override val description: String? = module.description
  override val icon: Icon? = if (!module.hasNotPassedLesson()) LearnIcons.checkMarkGray else null
  override val emptyIcon: Icon = EmptyIcon.ICON_0
  override fun isPopup(): Boolean = isExpanded

  override fun actionPerformed(e: AnActionEvent) {
    val lessonToOpen = module.giveNotPassedLesson() ?: module.lessons.first()
    StatisticBase.instance.onStartModuleAction(module, lessonToOpen)
    val openLessonAction = OpenLessonAction(lessonToOpen)
    ActionUtil.performActionDumbAware(openLessonAction, e)
  }
}