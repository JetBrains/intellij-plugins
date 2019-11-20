package training.ui.welcomeScreen.recentProjects.actionGroups

import training.ui.welcomeScreen.recentProjects.actionGroups.commonActions.DefaultProjectsActionGroup
import training.ui.welcomeScreen.recentProjects.actionGroups.commonActions.TutorialsActionGroup

object GroupManager {

    val projectsGroup = DefaultProjectsActionGroup().apply { isExpanded = true }
    val tutorialsGroup = TutorialsActionGroup().apply { isExpanded = true }

}