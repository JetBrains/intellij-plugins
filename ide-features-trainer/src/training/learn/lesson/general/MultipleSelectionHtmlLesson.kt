package training.learn.lesson.general

import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.util.PsiTreeUtil
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class MultipleSelectionHtmlLesson(module: Module) : KLesson("Multiple Selections", module, "HTML") {
  private val sample = parseLessonSample("""<!doctype html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Multiple selections</title>
    </head>
    <body>
        <table>
            <tr>
                <<caret>th>Firstname</th>
                <th>Lastname</th>
                <th>Points</th>
            </tr>
            <tr>
                <td>Eve</td>
                <td>Jackson</td>
                <td>94</td>
            </tr>
        </table>
    </body>
</html>
""".trimIndent())

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)

      triggerTask("SelectNextOccurrence") {
        text("Press ${action(it)} to select the symbol at the caret")
      }
      triggerTask("SelectNextOccurrence") {
        text("Press ${action(it)} again to select the next occurrence of this symbol")
      }
      triggerTask("UnselectPreviousOccurrence") {
        text("Press ${action(it)} to deselect the last occurrence")
      }
      triggerTask("SelectAllOccurrences") {
        text("Press ${action(it)} to select all occurrences in the file")
      }
      task {
        text("Type <code>td</code> to replace all occurrences of <code>th</code> with <code>td</code>")
        typeForTest("td")
        check({ Unit }, fun (_: Unit, _: Unit): Boolean {
          val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)

          val childrenOfType1 = PsiTreeUtil.findChildrenOfType(psiFile, HtmlTag::class.java)

          var count = 0

          for (htmlTag in childrenOfType1) {
            if (htmlTag.name == "th") return false
            if (htmlTag.name == "td") count++
          }
          return count == 6
        })
      }
    }
}