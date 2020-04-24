// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui.welcomeScreen.recentProjects

import com.intellij.ide.ProjectGroupActionGroup
import com.intellij.ide.RecentProjectListActionProvider
import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.components.ServiceManager
import training.ui.welcomeScreen.recentProjects.actionGroups.GroupManager

class IFTRecentProjectListActionProvider : RecentProjectListActionProvider() {

  fun getIFTActions(includeProjects: Boolean = true): List<AnAction> =
    if (includeProjects) {
      GroupManager.instance.registeredGroups
        .flatMap { it.getActions().toList() }
    }
    else {
      GroupManager.instance.registeredGroups
        .flatMap { it.getActions().toList() }
        .filter { it !is ReopenProjectAction && it !is ProjectGroupActionGroup }
    }

  fun getOriginalRecentProjectsActions(): Array<AnAction> = super.getActions(false, true).toTypedArray()

  val projectsWithLongPaths: MutableSet<ReopenProjectAction> = HashSet()

  companion object {
    val instance: IFTRecentProjectListActionProvider
      get() = ServiceManager.getService(IFTRecentProjectListActionProvider::class.java)
  }

}