package training.ui.welcomeScreen.recentProjects

import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.WelcomeFrameProvider

class IFTFlatWelcomeFrameProvider : WelcomeFrameProvider {
  override fun createFrame(): IdeFrame = IFTFlatWelcomeFrame()
}
