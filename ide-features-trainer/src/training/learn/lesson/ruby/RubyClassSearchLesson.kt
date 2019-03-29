package training.learn.lesson.ruby

import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.components.fields.ExtendableTextField
import training.commands.kotlin.TaskContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext

class RubyClassSearchLesson(module: Module) : KLesson("Class Search", module, "ruby") {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      caret(0)

      actionTask("GotoClass") {
        "Try to find a class with ${action(it)}"
      }
      task("date") {
        text("Type <code>$it</code> to see classes that contain the word <strong>$it</strong>")
        stateCheck { checkWordInSearch(it) }
        test {
          type(it)
        }
      }
      task("datebe") {
        text("You can search for a class by part of its name. Type <code>be</code> (the search string will be <code>$it</code>)" +
            "to see classes that contain the words <strong>date</strong> and <strong>be</strong>")
        stateCheck { checkWordInSearch(it) }
        test {
          type("be")
        }
      }
      task("QuickImplementations") {
        text("To check the selected class before navigating to it, you can use ${action(it)} to see its quick definition")
        trigger(it)
        test { actions(it) }
      }
    }

  private fun TaskContext.checkWordInSearch(expected: String): Boolean {
    val focusOwner = IdeFocusManager.getInstance(project).focusOwner
    return focusOwner is ExtendableTextField && focusOwner.text.toLowerCase() == expected.toLowerCase()
  }

  override val existedFile: String?
    get() = "lib/active_support.rb"
}
