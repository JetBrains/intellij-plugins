// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui.welcomeScreen.recentProjects

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.Constraints
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionConfigurationCustomizer
import training.actions.StartLearnAction

class IFTWelcomeScreenActionCustomizer : ActionConfigurationCustomizer {
  override fun customize(actionManager: ActionManager) {
    if (!showCustomWelcomeScreen) {
      val actionLinksGroup = actionManager.getAction("WelcomeScreen.QuickStart") as DefaultActionGroup
      if (actionLinksGroup.getChildren(null, actionManager).none { it is StartLearnAction }) {
        actionLinksGroup.addAction(StartLearnAction(), Constraints.LAST, actionManager)
      }
    }
  }
}
