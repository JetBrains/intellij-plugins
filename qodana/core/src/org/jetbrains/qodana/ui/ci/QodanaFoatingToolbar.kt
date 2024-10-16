package org.jetbrains.qodana.ui.ci

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.toolbar.floating.AbstractFloatingToolbarComponent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.observable.util.addComponent
import com.intellij.openapi.project.DumbAware
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import javax.swing.BorderFactory
import javax.swing.JPanel

class QodanaCopyFloatingToolbar(editor: EditorImpl) : JPanel() {
  init {
    val vgap = editor.component.height.minus(90)
    layout = FlowLayout(FlowLayout.RIGHT, 10, vgap)
    border = BorderFactory.createEmptyBorder()
    isOpaque = false

    val component = object : AbstractFloatingToolbarComponent(CopyActionGroup(), editor.disposable) {

      override val autoHideable: Boolean = false

      override fun isComponentOnHold(): Boolean = true

      override fun installMouseMotionWatcher() = Unit

      init {
        init(editor.contentComponent)
      }
    }

    component.scheduleShow()
    addComponent(component, editor.disposable)
  }
}

class CopyActionGroup : DefaultActionGroup() {
  val action = CopyAction()

  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    return arrayOf(action)
  }
}

class CopyAction : AnAction(), DumbAware {
  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun update(e: AnActionEvent) {
    e.presentation.icon = AllIcons.Actions.Copy
    e.presentation.isEnabledAndVisible = true
  }

  override fun actionPerformed(e: AnActionEvent) {
    val editor = e.dataContext.getData(CommonDataKeys.EDITOR) ?: return
    CopyPasteManager.getInstance().setContents(StringSelection(editor.document.text))
  }
}