package training.learn.lesson.java.completion

import training.lang.JavaLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class CompletionWithTabLesson(module: Module): KLesson("Completion with Tab", module, JavaLangSupport.lang) {

  val sample = parseLessonSample("""import javax.swing.*;

class FrameDemo {

    public static void main(String[] args) {
        JFrame frame = new JFrame("FrameDemo");
        frame.setSize(175, 100);

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
}""".trimIndent())

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    caret(9, 56)
    actionTask("CodeCompletion") {
      "Press ${action(it)} to show completion options."
    }
    actionTask("EditorChooseLookupItemReplace") {
      "Choose <code>DO_NOTHING_ON_CLOSE</code>, for example, and press ${action("EditorTab")}. This overwrites the word at the caret rather than simply inserting it."
    }
  }
}