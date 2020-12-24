package name.kropp.intellij.makefile.toolWindow

import com.intellij.icons.*
import com.intellij.openapi.actionSystem.*
import javax.swing.tree.*

class MakefileToolWindowSortAlphabeticallyAction(private val options: MakefileToolWindowOptions, private val model: DefaultTreeModel) :
    ToggleAction("Sort alphabetically", null, AllIcons.ObjectBrowser.Sorted) {
  override fun isSelected(e: AnActionEvent): Boolean = options.sortAlphabetically

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    options.sortAlphabetically = state
    model.setRoot(options.getRootNode())
  }
}