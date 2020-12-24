package name.kropp.intellij.makefile.toolWindow

import com.intellij.icons.*
import com.intellij.openapi.actionSystem.*
import com.intellij.ui.*
import javax.swing.*

class MakefileToolWindowAutoscrollToSourceAction(
    private val options: MakefileToolWindowOptions,
    private val autoScrollHandler: AutoScrollToSourceHandler,
    private val tree: JComponent)
  : ToggleAction("Autoscroll to source", null, AllIcons.General.AutoscrollToSource) {

  override fun isSelected(e: AnActionEvent): Boolean = options.autoScrollToSource

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    options.autoScrollToSource = state
    if (state) {
      autoScrollHandler.onMouseClicked(tree)
    }
  }
}