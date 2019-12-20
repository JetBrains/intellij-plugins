package training.ui.welcomeScreen.recentProjects

import com.intellij.ide.ProjectGroupActionGroup
import com.intellij.ide.RecentProjectListActionProvider
import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.actionSystem.AnAction
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
      get() = getInstance() as IFTRecentProjectListActionProvider
  }

}