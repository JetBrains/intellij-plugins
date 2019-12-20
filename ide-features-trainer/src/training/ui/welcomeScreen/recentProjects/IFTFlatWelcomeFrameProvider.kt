// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui.welcomeScreen.recentProjects

import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.WelcomeFrameProvider

class IFTFlatWelcomeFrameProvider : WelcomeFrameProvider {
  override fun createFrame(): IdeFrame = IFTFlatWelcomeFrame()
}
