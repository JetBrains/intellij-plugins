package training.ui.welcomeScreen.recentProjects

import com.intellij.ide.RecentDirectoryProjectsManager
import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.actionSystem.AnAction
import training.ui.welcomeScreen.recentProjects.actionGroups.GroupManager
import java.util.HashSet

class IFTRecentProjectsManager : RecentDirectoryProjectsManager() {

    override fun getRecentProjectsActions(forMainMenu: Boolean, useGroups: Boolean): Array<AnAction?> {
        return if (!useGroups)
            super.getRecentProjectsActions(forMainMenu, false)
        else {
          GroupManager.instance.registeredGroups.flatMap { it.getActions().toList() }.toTypedArray()
        }
    }

  fun getOriginalRecentProjectsActions(): Array<AnAction?> {
    return super.getRecentProjectsActions(forMainMenu = false, useGroups = true)
  }

  override fun getRecentProjectsActions(forMainMenu: Boolean): Array<AnAction?> {
    return getRecentProjectsActions(forMainMenu, false)
  }

  val projectsWithLongPaths: HashSet<ReopenProjectAction> by lazy {
    HashSet<ReopenProjectAction>(0)
  }

}