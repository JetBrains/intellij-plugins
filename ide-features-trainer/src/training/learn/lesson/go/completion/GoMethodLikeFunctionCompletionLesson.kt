package training.learn.lesson.go.completion

import com.goide.psi.GoFile
import com.intellij.psi.PsiDocumentManager
import org.intellij.lang.annotations.Language
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class GoMethodLikeFunctionCompletionLesson(module: Module) : GoLesson("Method-Like Function Completion", module) {

  @Language("go")
  val sample = parseLessonSample("""package main

func main() {
	"hello world".<caret>
}
""")

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    task {
      text("To find all functions which accept an element as the first argument press ${action("CodeCompletion")} <strong>twice</strong>. " +
              "Notice that that the dot makes it look like a method while it's not.")
      triggers("CodeCompletion", "CodeCompletion")
    }
    task {
      text("Choose <code>Split</code> (package <code>path</code>) from the list and hit ${action("EditorEnter")} " +
              "(you can start typing to reduce the number of results).")
      trigger("EditorChooseLookupItem") {
        val manager = PsiDocumentManager.getInstance(project)
        val file = manager.getPsiFile(editor.document) as? GoFile ?: return@trigger false
        val block = file.functions.find { it.name == "main" }?.block ?: return@trigger false
        block.statementList.any { it.text == """path.Split("hello world")""" }
      }
    }
  }

}