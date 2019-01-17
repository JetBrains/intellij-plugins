package training.learn.lesson.general

import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample

abstract class SingleLineCommentLesson(module: Module, lang: String) :
    KLesson("Comment Line", module, lang) {

  abstract val commentElementType : IElementType
  abstract val sample: LessonSample

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      fun countCommentedLines() : Int =
          calculateComments(PsiDocumentManager.getInstance(project).getPsiFile(editor.document)!!)

      prepareSample(sample)

      triggerTask("CommentByLineComment") {
        text("Comment out any line with ${action(it)}")
      }
      triggerTask("CommentByLineComment") {
        text("Uncomment the commented line with the same shortcut, ${action(it)}.")
        check({countCommentedLines()}, { _, now -> now == 0 } )
      }
      task {
        text("Select several lines with ${action("EditorDownWithSelection")} " +
            "and then comment them with ${action("CommentByLineComment")}.")
        trigger("EditorDownWithSelection")
        testActions("EditorDownWithSelection", "EditorDownWithSelection")
        trigger("CommentByLineComment")
        check({countCommentedLines()}, { before, now -> now >= before + 2 } )
      }
    }

  private fun calculateComments(psiElement: PsiElement): Int {
    return when {
      psiElement.node.elementType === commentElementType -> 1
      psiElement.children.isEmpty() -> 0
      else -> {
        var result = 0
        for (psiChild in psiElement.children) {
          result += calculateComments(psiChild)
        }
        result
      }
    }
  }
}
