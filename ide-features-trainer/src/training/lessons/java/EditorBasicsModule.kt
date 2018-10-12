package training.lessons.java

import training.commands.kotlin.*
import training.learn.interfaces.ModuleType
import training.learn.lesson.kimpl.KModule
import training.learn.lesson.kimpl.lesson

class EditorBasicsModule : KModule("Editor Basics Kotlin", ModuleType.SCRATCH) {

  fun selectLesson() = lesson("Select", "JAVA") {
    task {
      copyCode("""class SelectionDemo {

    public int fib(int n) {
        int a = 1;
        int b = 1;

        int tmp;

        if (n < 2) return 1;

        for (int i = 0; i < (n - 1); i++) {
            tmp = b;
            b = a;
            a = a + tmp;
        }

        return a;
    }
}""")
      text("Place the caret before any word. Press ${action("EditorNextWordWithSelection")} to move the caret to the next word and select everything in between.")
      caret(line = 9, column = 9)
      trigger("EditorNextWordWithSelection")
    }
    task {
      text("Press ${action("EditorSelectWord")} to extend the selection to the next code block.")
      trigger("EditorSelectWord")
    }
    task {
      text("Try increasing your selection with <action>EditorSelectWord</action> until your whole file is selected.")
      trigger("EditorSelectWord")
    }
    task {
      text("${action("EditorUnSelectWord")} shrinks selection. Try pressing it.")
      trigger("EditorUnSelectWord")
    }
    task {
      text("Now select the whole file instantly with ${action("\$SelectAll")}.")
      trigger("\$SelectAll")
    }
    task {
      text("Awesome! Click the button below to start the next lesson, or use ${action("learn.next.lesson")}.")
      trigger("learn.next.lesson")
    }
//    complete()
  }

}