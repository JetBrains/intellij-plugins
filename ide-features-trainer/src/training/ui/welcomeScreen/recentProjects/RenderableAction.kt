package training.ui.welcomeScreen.recentProjects

import com.intellij.openapi.actionSystem.AnAction
import javax.swing.Icon

interface RenderableAction {
  val action: AnAction
  val name: String
  val description: String?
  val icon: Icon?
  val emptyIcon: Icon
  var isValid: Boolean
}