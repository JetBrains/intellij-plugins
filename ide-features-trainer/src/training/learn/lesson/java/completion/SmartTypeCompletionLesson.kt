/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.lesson.java.completion

import training.lang.JavaLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class SmartTypeCompletionLesson(module: Module) : KLesson("Smart Type Completion", module, JavaLangSupport.lang) {

  val sample = parseLessonSample("""import java.lang.String;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

class SmartCompletionDemo{

    private Queue<String> strings;
    private ArrayBlockingQueue<String> arrayBlockingQueue;

    public SmartCompletionDemo(LinkedList<String> linkedList, HashSet<String> hashSet) {
        strings =
        arrayBlockingQueue = new ArrayBlockingQueue<String>(hashSet.size());
        for (String s : hashSet)
            arrayBlockingQueue.add(s);
    }

    private String[] toArray() {
        return <caret>
    }

}""".trimIndent())

  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        prepareSample(sample)
        task {
          caret(13, 19)
          text("Smart Type Completion filters the list of suggestion to include only those types that are applicable in the current context. Press ${action("SmartTypeCompletion")} to see the list of matching suggestions. Choose the first one by pressing ${action("EditorEnter")}.")
          trigger("SmartTypeCompletion")
          trigger("EditorChooseLookupItem")
        }
        task {
          caret(20, 16)
          text("Smart Type Completion can also suggest code for a return statement. Press ${action("SmartTypeCompletion")} twice to see the Lookup menu for a return. Choose the first one by pressing ${action("EditorEnter")}")
          triggers("SmartTypeCompletion", "SmartTypeCompletion")
          trigger("EditorChooseLookupItem")
        }
      }
    }

}