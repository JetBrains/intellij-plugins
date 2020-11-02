// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.java.completion

import training.lang.JavaLangSupport
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class JavaPostfixCompletionLesson(module: Module)
  : KLesson("Postfix completion", LessonsBundle.message("postfix.completion.lesson.name"), module, JavaLangSupport.lang) {

  val sample = parseLessonSample("""
    class PostfixCompletionDemo{
        public void demonstrate(int show_times){
            (show_times == 10)
        }
    }
  """.trimIndent())

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    caret(4, 27)
    actionTask("EditorChooseLookupItem") {
      LessonsBundle.message("java.postfix.completion.apply", code("."), code("if"), action("EditorChooseLookupItem"))
    }
  }
}