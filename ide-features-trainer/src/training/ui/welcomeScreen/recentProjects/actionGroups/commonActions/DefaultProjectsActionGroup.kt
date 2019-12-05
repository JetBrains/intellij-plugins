package training.ui.welcomeScreen.recentProjects.actionGroups.commonActions

import com.intellij.openapi.actionSystem.AnAction
import training.ui.welcomeScreen.recentProjects.IFTRecentProjectListActionProvider
import training.ui.welcomeScreen.recentProjects.actionGroups.CommonActionGroup

class DefaultProjectsActionGroup : CommonActionGroup("Projects", emptyList()) {

  override fun getActions(): Array<AnAction> {
    val elements = IFTRecentProjectListActionProvider.instance.getOriginalRecentProjectsActions()
    return when {
      elements.isEmpty() -> emptyArray()
      !isExpanded -> arrayOf(this)
      else -> arrayOf(this, *elements)
    }
  }
}