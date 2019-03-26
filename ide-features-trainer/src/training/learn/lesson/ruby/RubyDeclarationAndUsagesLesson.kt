package training.learn.lesson.ruby

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testGuiFramework.framework.GuiTestUtil.shortcut
import com.intellij.testGuiFramework.framework.Timeouts
import com.intellij.testGuiFramework.impl.waitUntilFound
import com.intellij.testGuiFramework.util.Key
import com.intellij.ui.table.JBTable
import training.commands.kotlin.TaskContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext

class RubyDeclarationAndUsagesLesson(module: Module) : KLesson("Declaration and usages", module, "ruby") {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      caret(20, 45)

      task("GotoDeclaration") {
        text("Use ${action(it)} to jump to the declaration of a attribute accessor")
        trigger(it, { state() }) { before, _ ->
          before != null && !isInsidePsi(before.target.navigationElement, before.position)
        }
        test { actions(it) }
      }

      task("GotoDeclaration") {
        text("Now the editor caret is on attribute accessor declaration. Use the same shortcut ${action(it)}" +
            "to see all its usages and select one of them.")
        trigger(it, { state() }, fun(before: MyInfo?, now: MyInfo?): Boolean {
          if (before == null || now == null) {
            return false
          }

          val navigationElement = before.target.navigationElement
          return navigationElement == now.target.navigationElement &&
              isInsidePsi(navigationElement, before.position) &&
              !isInsidePsi(navigationElement, now.position)
        })
        test {
          actions(it)
          ideFrame {
            waitUntilFound(null, JBTable::class.java, Timeouts.seconds02) { popup ->
              popup.javaClass.name.contains("ShowUsagesTable") && popup.isShowing
            }
            shortcut(Key.ENTER)
          }
        }
      }
      actionTask("FindUsages") {
        "Use ${action(it)} to see more detailed view of usages. Note: it could be called from either some usage or declaration itself."
      }

      actionTask("PinToolwindowTab") {
        "From <strong>Find view</strong> you could navigate to any usage or declaration itself. " +
            "Any next search will override these results in <strong>Find view</strong>. " +
            "To prevent it pin them in menu on right mouse click over <strong>Usages of</strong> tab."
      }

      actionTask("HideActiveWindow") {
        "When you finish to browse usages use ${action(it)} to hide last focused view"
      }

      actionTask("ActivateFindToolWindow") {
        "Press ${action(it)} to open <strong>Find view</strong> again"
      }
    }

  private fun TaskContext.state(): MyInfo? {
    val flags = TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED

    val currentEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return null

    val target = TargetElementUtil.findTargetElement(currentEditor, flags) ?: return null

    val file = PsiDocumentManager.getInstance(project).getPsiFile(currentEditor.document) ?: return null
    val position = MyPosition(file, currentEditor.caretModel.offset)

    return MyInfo(target, position)
  }

  private fun isInsidePsi(psi: PsiElement, position: MyPosition) : Boolean {
    return psi.containingFile == position.file && psi.textRange.contains(position.offset)
  }

  private data class MyInfo(val target: PsiElement, val position: MyPosition)

  private data class MyPosition(val file: PsiFile, val offset: Int)

  override val existedFile: String?
    get() = "lib/active_support/core_ext/date/calculations.rb"
}
