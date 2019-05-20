package training.learn.lesson.go.completion

import org.intellij.lang.annotations.Language
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class GoPostfixCompletionLesson(module: Module) : GoLesson("Postfix Completion", module) {

  @Language("go")
  val sample = parseLessonSample("""package main

import (
	"fmt"
	"strings"
)

func main() {
	message := []string{"world", "hello"}
	message<caret>
	fmt.Printf(strings.Join(message, " "))
}
""")

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    actionTask("EditorChooseLookupItem") {
      "Postfix Completion helps reduce backward caret jumps as you write code. It lets you transform an already typed expression into another one based on the postfix you add, the type of expression, and its context. Type <code>.</code> after the <code>message</code> variable to see the list of postfix completion suggestions. Select <code>sort</code> from the list, or type it in editor, and then press ${action("EditorEnter")} to complete the statement."
    }
  }

}