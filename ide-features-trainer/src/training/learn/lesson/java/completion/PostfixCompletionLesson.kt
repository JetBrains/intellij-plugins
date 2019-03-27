package training.learn.lesson.java.completion

import training.lang.JavaLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class PostfixCompletionLesson(module: Module): KLesson("Postfix Completion", module, JavaLangSupport.lang) {

  val sample = parseLessonSample("""class PostfixCompletionDemo{

    public void demonstrate(int show_times){
        (show_times == 10)
    }
}""".trimIndent())

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    caret(4, 27)
    actionTask("EditorChooseLookupItem") {
      "Postfix Completion helps reduce backward caret jumps as you write code. It lets you transform an already typed expression into another one based on the postfix you add, the type of expression, and its context. Type <code>.</code> after the parenthesis to see the list of postfix completion suggestions. Select <code>if</code> from the list, or type it in editor, and then press ${action("EditorEnter")} to complete the statement."
    }
  }

}