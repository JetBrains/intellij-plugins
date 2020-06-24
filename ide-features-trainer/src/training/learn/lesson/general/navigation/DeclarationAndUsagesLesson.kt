// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.general.navigation

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.openapi.actionSystem.impl.ActionMenuItem
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.wm.impl.content.BaseLabel
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testGuiFramework.framework.GuiTestUtil.shortcut
import com.intellij.testGuiFramework.util.Key
import com.intellij.ui.table.JBTable
import training.commands.kotlin.TaskRuntimeContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext

abstract class DeclarationAndUsagesLesson(module: Module, lang: String) : KLesson("Declaration and usages", module, lang) {
  abstract fun LessonContext.setInitialPosition()
  abstract val typeOfEntity: String
  abstract override val existedFile: String

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      setInitialPosition()

      task("GotoDeclaration") {
        text("Use ${action(it)} to jump to the declaration of $typeOfEntity")
        trigger(it, { state() }) { before, _ ->
          before != null && !isInsidePsi(before.target.navigationElement, before.position)
        }
        test { actions(it) }
      }

      task("GotoDeclaration") {
        text("Now the caret is on the attribute accessor declaration. " +
             "Use the same shortcut ${action(it)} to see all of its usages, then select one of them.")
        trigger(it, { state() }) l@{ before, now ->
          if (before == null || now == null) {
            return@l false
          }

          val navigationElement = before.target.navigationElement
          return@l navigationElement == now.target.navigationElement &&
                   isInsidePsi(navigationElement, before.position) &&
                   !isInsidePsi(navigationElement, now.position)
        }
        test {
          actions(it)
          ideFrame {
            waitComponent(JBTable::class.java, "ShowUsagesTable")
            shortcut(Key.ENTER)
          }
        }
      }

      task("FindUsages") {
        text("Use ${action(it)} to see a more detailed view of usages. You can invoke ${action(it)} on either a declaration or usage.")

        triggerByUiComponentAndHighlight { ui: BaseLabel ->
          ui.text?.contains("Usages of") ?: false
        }
        test {
          actions(it)
        }
      }

      task {
        test {
          ideFrame {
            previous.ui?.let { usagesTab -> jComponent(usagesTab).rightClick() }
          }
        }
        triggerByUiComponentAndHighlight(highlightInside = false) { ui: ActionMenuItem ->
          ui.text?.contains("Pin Tab") ?: false
        }
        restoreByUi()
        text("From the <strong>Find view</strong> you can navigate to both usages and declarations. " +
             "The next search will override these results in the <strong>Find view</strong> window. " +
             "To prevent it, pin the results: ")
        text("Right click the tab title, <strong>Usages of</strong>.")
      }

      task("PinToolwindowTab") {
        trigger(it)
        restoreByUi()
        text("Select <strong>Pin tab</strong>.")
        test {
          ideFrame {
            jComponent(previous.ui!!).click()
          }
        }
      }

      actionTask("HideActiveWindow") {
        "When you have finished browsing usages, use ${action(it)} to hide the view."
      }

      actionTask("ActivateFindToolWindow") {
        "Press ${action(it)} to open the <strong>Find view</strong> again."
      }
    }

  private fun TaskRuntimeContext.state(): MyInfo? {
    val flags = TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED

    val currentEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return null

    val target = TargetElementUtil.findTargetElement(currentEditor, flags) ?: return null

    val file = PsiDocumentManager.getInstance(project).getPsiFile(currentEditor.document) ?: return null
    val position = MyPosition(file,
                              currentEditor.caretModel.offset)

    return MyInfo(target, position)
  }

  private fun isInsidePsi(psi: PsiElement, position: MyPosition): Boolean {
    return psi.containingFile == position.file && psi.textRange.contains(position.offset)
  }

  private data class MyInfo(val target: PsiElement, val position: MyPosition)

  private data class MyPosition(val file: PsiFile, val offset: Int)
}
