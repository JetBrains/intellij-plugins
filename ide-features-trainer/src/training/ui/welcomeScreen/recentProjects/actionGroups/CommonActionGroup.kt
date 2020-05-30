// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui.welcomeScreen.recentProjects.actionGroups

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.impl.welcomeScreen.RecentProjectsWelcomeScreenActionBase
import training.statistic.StatisticBase

abstract class CommonActionGroup(val name: String, val children: List<AnAction>) : DefaultActionGroup(name, children), DumbAware {

  var isExpanded = true

  override fun actionPerformed(e: AnActionEvent) {
    object : RecentProjectsWelcomeScreenActionBase() {
      override fun actionPerformed(e: AnActionEvent) {
        isExpanded = !isExpanded
        val list = getList(e)
        if (list != null) {
          val index = list.selectedIndex
          rebuildRecentProjectsList(e)
          list.selectedIndex = index
        }
        StatisticBase.instance.onGroupStateChanged(name, isExpanded)
      }
    }.actionPerformed(e)
  }

  abstract fun getActions(): Array<AnAction>

  override fun isPopup(): Boolean {
    return !isExpanded
  }

  override fun toString(): String {
    return name
  }
}