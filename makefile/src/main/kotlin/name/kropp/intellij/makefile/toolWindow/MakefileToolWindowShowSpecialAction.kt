package name.kropp.intellij.makefile.toolWindow

import com.intellij.icons.*
import com.intellij.openapi.actionSystem.*
import javax.swing.tree.*

class MakefileToolWindowShowSpecialAction(private val options: MakefileToolWindowOptions, private val model: DefaultTreeModel) :
    ToggleAction("Show special targets", null, AllIcons.Actions.ShowHiddens) {
  override fun isSelected(e: AnActionEvent): Boolean = options.showSpecialTargets

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    options.showSpecialTargets = state
    model.setRoot(options.getRootNode())
  }
}
