// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.ruby.refactorings

import com.intellij.openapi.wm.IdeFrame
import com.intellij.testGuiFramework.impl.button
import com.intellij.ui.ComponentUtil
import com.intellij.util.ui.UIUtil
import training.commands.kotlin.TaskTestContext
import training.lang.RubyLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample
import javax.swing.JDialog
import javax.swing.JTextPane

class RubyExtractMethodLesson(module: Module) : KLesson("Extract Method", module, "ruby") {
  private val sample = parseLessonSample("""
    def cocktail_sort(array, compare = lambda { |a, b| a <=> b })
      loop do
        swapped = false
        0.upto(array.length - 2) do |i|
          if compare.call(array[i], array[i + 1]) > 0
            <select>array[i], array[i + 1] = array[i + 1], array[i]</select>
            swapped = true
          end
        end
        break unless swapped

        swapped = false
        (array.length - 2).downto(0) do |i|
          if compare.call(array[i], array[i + 1]) > 0
            array[i], array[i + 1] = array[i + 1], array[i]
            swapped = true
          end
        end
        break unless swapped
      end
    end
  """.trimIndent())

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
