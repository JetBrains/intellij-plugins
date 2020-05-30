// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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