package training.ui.welcomeScreen.recentProjects

import com.intellij.ide.RecentDirectoryProjectsManager
import com.intellij.ide.RecentProjectsManager
import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.util.registry.Registry
import training.ui.welcomeScreen.recentProjects.actionGroups.GroupManager
import java.util.*

class IFTRecentProjectsManager : RecentDirectoryProjectsManager() {

  override fun getRecentProjectsActions(forMainMenu: Boolean, useGroups: Boolean): Array<AnAction?> {
    if (!Registry.`is`("ideFeaturesTrainer.welcomeScreen.tutorialsTree"))
      return super.getRecentProjectsActions(forMainMenu, useGroups)
    return if (!useGroups)
      super.getRecentProjectsActions(forMainMenu, false)
    else {
      GroupManager.instance.registeredGroups.flatMap { it.getActions().toList() }.toTypedArray()
    }
  }

  override fun getRecentProjectsActions(forMainMenu: Boolean): Array<AnAction?> {
    return getRecentProjectsActions(forMainMenu, false)
  }

  fun getOriginalRecentProjectsActions(): Array<AnAction> {
    return super.getRecentProjectsActions(forMainMenu = false, useGroups = true).filterNotNull().toTypedArray()
  }

  val projectsWithLongPaths: HashSet<ReopenProjectAction> by lazy {
    HashSet<ReopenProjectAction>(0)
  }

  companion object {
    val manager: IFTRecentProjectsManager
      get() = RecentProjectsManager.getInstance() as IFTRecentProjectsManager
  }

}