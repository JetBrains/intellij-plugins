// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.general.refactorings

import com.intellij.openapi.wm.IdeFrame
import com.intellij.testGuiFramework.impl.button
import com.intellij.ui.ComponentUtil
import com.intellij.util.ui.UIUtil
import training.commands.kotlin.TaskTestContext
import training.lang.RubyLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import javax.swing.JDialog
import javax.swing.JTextPane

class ExtractMethodCocktailSortLesson(module: Module, lang: String, private val sample: LessonSample) : KLesson("Extract Method", module, lang) {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)

      actionTask("ExtractMethod") {
        "Press ${action(it)} to extract the selected code block into a method."
      }
      // Now will be open the first dialog

      task {
        text("Click <strong>Ok</strong> to start refactoring.")

        // Wait until the second dialog
        stateCheck {
          val parentOfType = ComponentUtil.getParentOfType(JDialog::class.java, focusOwner)
          // Only the second dialog has JTextPane. We do not need to check title
          UIUtil.uiTraverser(parentOfType).traverse().filter(JTextPane::class.java).first() != null
        }

        test {
          with(TaskTestContext.guiTestCase) {
            dialog("Extract Method", needToKeepDialog=true) {
              button("OK").click()
            }
          }
        }
      }

      task {
        text("Cocktail Sort has 2 swap places. The first fragment has just been extracted. Click <strong>Yes</strong> to extract both of them.")

        // Wait until the third dialog
        stateCheck {
          val parentOfType = ComponentUtil.getParentOfType(JDialog::class.java, focusOwner)
          // Only the second dialog has JTextPane. We do not need to check title
          parentOfType?.title == "Replace Fragment"
        }

        test {
          with(TaskTestContext.guiTestCase) {
            dialog("Extract Method") {
              button("Yes").click()
            }
          }
        }
      }
      task {
        text("Now you can confirm or reject replacement of the second fragment.")

        stateCheck {
          ComponentUtil.getParentOfType(IdeFrame::class.java, focusOwner) != null
        }

        test {
          with(TaskTestContext.guiTestCase) {
            dialog("Replace Fragment") {
              button("Replace").click()
            }
          }
        }
      }
    }

  override val existedFile = RubyLangSupport.sandboxFile
}
