package training.learn.lesson.java.completion

import training.lang.JavaLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class BasicCompletionLesson(module: Module) : KLesson("Basic Completion", module, JavaLangSupport.lang) {

  val sample = parseLessonSample("""import java.lang.*;
import java.util.*;

class BasicCompletionDemo implements Runnable{

    private int i = 0;

    public void systemProcess(){
        System.out.println(i++);
    }

    public BasicCompletionDemo() {
        byte b = MAX_VALUE
    }

    @Override
    public void run() {
        Random random = new <caret>
    }
}""".trimIndent())


  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        prepareSample(sample)
        actionTask("EditorChooseLookupItem") {
          "By default, IntelliJ IDEA completes your code instantly. Start typing <code>Ran</code> right where the caret is, and you will see the Lookup Menu with matching suggestions. You can choose the first item from the Lookup menu by pressing ${action("EditorEnter")}."
        }
        task("CodeCompletion") {
          caret(18, 36)
          text("To activate Basic Completion, press ${action(it)} and you will see lookup menu again.")
          trigger(it)
        }
        actionTask("EditorChooseLookupItem") {
          "Select <code>i</code> inside the lookup menu and press <action>EditorEnter</action>."
        }
        actionTask("EditorCompleteStatement") {
          "Press ${action(it)} to complete this statement."
        }
        task("CodeCompletion") {
          caret(13, 27)
          text("Sometimes you need to see suggestions for static constants or methods. Press ${action(it)} twice to access a deeper level of Code Completion.")
          triggers(it, it)
        }

      }
    }


}