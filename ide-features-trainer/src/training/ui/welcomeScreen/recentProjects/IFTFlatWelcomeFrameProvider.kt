package training.ui.welcomeScreen.recentProjects

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.Constraints
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.WelcomeFrameProvider
import training.actions.StartLearnAction

class IFTFlatWelcomeFrameProvider : WelcomeFrameProvider {
  override fun createFrame(): IdeFrame {
    if (!Registry.`is`("ideFeaturesTrainer.welcomeScreen.tutorialsTree")) {
      val actionLinksGroup = ActionManager.getInstance().getAction("WelcomeScreen.QuickStart") as DefaultActionGroup
      if (actionLinksGroup.getChildren(null).any { it is StartLearnAction }.not())
        actionLinksGroup.addAction(StartLearnAction(), Constraints.LAST, ActionManager.getInstance())
    }
    return IFTFlatWelcomeFrame()
  }
}
