/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.lesson.ruby.refactorings

import com.intellij.testGuiFramework.impl.button
import training.commands.kotlin.TaskTestContext
import training.lang.RubyLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

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

      task("ExtractMethod") {
        text("Press ${action(it)} to extract the selected code block into a method.")
        trigger(it, { editor.document.text }) { before, now->
          if (before != now) {
            // a little bit hacky way to print additional message between refactoring modal dialogs
            text("Cocktail Sort has 2 swap places. Click <strong>Yes</strong> to extract both of them and " +
                "then confirm the decision.")
            true
          }
          else {
            false
          }
        }

        test {
          actions(it)
          with(TaskTestContext.guiTestCase) {
            dialog("Extract Method") {
              button("OK").click()
              dialog("Extract Method") {
                button("Yes").click()
              }
            }
            dialog("Replace Fragment") {
              button("Replace").click()
            }
          }
        }
      }
    }

  override val existedFile = RubyLangSupport.sandboxFile
}
