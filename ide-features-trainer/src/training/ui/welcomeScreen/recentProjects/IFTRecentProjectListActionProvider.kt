package training.ui.welcomeScreen.recentProjects

import com.intellij.ide.RecentProjectListActionProvider
import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.actionSystem.AnAction
import training.ui.welcomeScreen.recentProjects.actionGroups.GroupManager

class IFTRecentProjectListActionProvider : RecentProjectListActionProvider() {

  override fun getActions(addClearListItem: Boolean, useGroups: Boolean): List<AnAction> =
    when {
      !showCustomWelcomeScreen -> super.getActions(addClearListItem, useGroups)
      !useGroups -> super.getActions(addClearListItem, false)
      else -> GroupManager.instance.registeredGroups.flatMap { it.getActions().toList() }
    }

  fun getOriginalRecentProjectsActions(): Array<AnAction> = super.getActions(false, true).toTypedArray()

  val projectsWithLongPaths: MutableSet<ReopenProjectAction> = HashSet()

  companion object {
    val instance: IFTRecentProjectListActionProvider
      get() = getInstance() as IFTRecentProjectListActionProvider
  }

}