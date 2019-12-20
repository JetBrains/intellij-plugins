package training.ui.welcomeScreen.recentProjects

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.Constraints
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.WelcomeFrameProvider
import training.actions.StartLearnAction

class IFTFlatWelcomeFrameProvider : WelcomeFrameProvider {
  override fun createFrame(): IdeFrame {
    if (!showCustomWelcomeScreen) {
      val actionLinksGroup = ActionManager.getInstance().getAction("WelcomeScreen.QuickStart") as DefaultActionGroup
      if (actionLinksGroup.getChildren(null).none { it is StartLearnAction }) {
        actionLinksGroup.addAction(StartLearnAction(), Constraints.LAST, ActionManager.getInstance())
      }
    }
    return IFTFlatWelcomeFrame()
  }
}
