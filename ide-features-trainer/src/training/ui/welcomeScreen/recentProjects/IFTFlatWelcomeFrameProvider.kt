// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui.welcomeScreen.recentProjects

import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.WelcomeFrameProvider
import com.intellij.openapi.wm.impl.welcomeScreen.FlatWelcomeFrame
import training.actions.StartLearnAction
import training.ui.welcomeScreen.recentProjects.actionGroups.GroupManager

private class IFTFlatWelcomeFrameProvider : WelcomeFrameProvider {
  init {
    StartLearnAction.updateState(GroupManager.instance.showTutorialsOnWelcomeFrame)
  }

  override fun createFrame(): IdeFrame? {
    return if (FlatWelcomeFrame.USE_TABBED_WELCOME_SCREEN) null else IFTFlatWelcomeFrame()
  }
}
