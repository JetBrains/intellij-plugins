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
    if (Registry.`is`("ideFeaturesTrainer.welcomeScreen.showLearnAction")) {
      val groupQuickStart = ActionManager.getInstance().getAction("WelcomeScreen.QuickStart")
      (groupQuickStart as DefaultActionGroup).addAction(StartLearnAction(), Constraints.LAST, ActionManager.getInstance())
    }
    return IFTFlatWelcomeFrame()
  }
}
