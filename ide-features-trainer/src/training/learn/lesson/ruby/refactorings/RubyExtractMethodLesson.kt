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
    class VeryStupidExample
      def func(a)
        b = 10
    <select>    x = a + b
    </select>    x + 1
      end
    end
  """.trimIndent())

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      task("ExtractMethod") {
        text("Press ${action(it)} to extract the selected code block into a method.")
        trigger(it, { editor.document.text }) { before, now->
          before != now
        }

        test {
          actions(it)
          with(TaskTestContext.guiTestCase) {
            dialog("Extract Method") {
              button("OK").click()
            }
          }
        }
      }
    }

  override val existedFile = RubyLangSupport.sandboxFile
}
