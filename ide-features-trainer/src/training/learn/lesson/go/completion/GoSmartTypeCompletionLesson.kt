package training.learn.lesson.go.completion

import org.intellij.lang.annotations.Language
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class GoSmartTypeCompletionLesson(module: Module) : GoLesson("Smart Type Completion", module) {

  @Language("go")
  val sample = parseLessonSample("""package main

import "fmt"

func main() {
	message := "hello world"
	fmt.Printf(<caret>)
}
""")

  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        prepareSample(sample)
        task {
          text("Smart Type Completion filters the list of suggestion to include only those types that are applicable in the current context. Press ${action("SmartTypeCompletion")} to see the list of matching suggestions. Choose the first one by pressing ${action("EditorEnter")}.")
          trigger("SmartTypeCompletion")
          trigger("EditorChooseLookupItem")
        }
      }
    }

}