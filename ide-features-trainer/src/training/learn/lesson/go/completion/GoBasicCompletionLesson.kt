package training.learn.lesson.go.completion

import org.intellij.lang.annotations.Language
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class GoBasicCompletionLesson(module: Module) : GoLesson("Basic Completion", module) {

  @Language("go")
  val sample = parseLessonSample("""package main

func main() {
    message := "hello world"
	<caret>
}
""")

  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        prepareSample(sample)
        actionTask("EditorChooseLookupItem") {
          "By default, GoLand completes your code instantly. Start typing <code>fmtpri</code> right where the caret is, and you will see the Lookup Menu with matching suggestions. You can choose <code>fmt.Printf</code> from the Lookup menu by pressing ${action("EditorEnter")}."
        }
        caret(7, 16)
        actionTask("CodeCompletion") {
          "Notice that GoLand inserts a new import automatically. Now, to activate Basic Completion, press ${action(it)} and you will see lookup menu again."
        }
        actionTask("EditorChooseLookupItem") {
          "Select <code>message</code> inside the lookup menu and press <action>EditorEnter</action>."
        }
        actionTask("EditorCompleteStatement") {
          "Press ${action(it)} to complete this statement and move to a new line."
        }
      }
    }

}