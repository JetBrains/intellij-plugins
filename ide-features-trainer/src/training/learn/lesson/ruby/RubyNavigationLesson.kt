package training.learn.lesson.ruby

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext

class RubyNavigationLesson(module: Module) : KLesson("Basic Navigation", module, "ruby") {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      caret(20, 45)

      actionTask("GotoDeclaration") {
        "Use ${action(it)} to jump to the declaration of a class or interface."
      }

      actionTask("GotoClass") {
        "Try to find a class with ${action(it)}"
      }
      task {
        text("Write 'Us' and press ${action("QuickImplementations")} to see the definition of the selected class")
        typeForTest("Us")
        trigger("QuickImplementations")
      }
    }

  override val existedFile: String?
    get() = "lib/active_support/core_ext/date/calculations.rb"
}
