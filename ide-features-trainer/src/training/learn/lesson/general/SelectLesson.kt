// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.general

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample

class SelectLesson(module: Module, lang: String, private val sample: LessonSample) :
  KLesson("Select", "Expand and shrink the code selection", module, lang) {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)

      actionTask("EditorNextWordWithSelection") {
        "Place the caret before any word. Press ${action(it)} to move the caret to the next word and select everything in between."
      }
      actionTask("EditorSelectWord") {
        "Press ${action(it)} to extend the selection to the next code block."
      }
      task("EditorSelectWord") {
        text("Try increasing your selection with ${action(it)} until your whole file is selected.")
        trigger(it) {
          editor.selectionModel.selectionStart == 0 && editor.document.textLength == editor.selectionModel.selectionEnd
        }
        test {
          for (i in 1..7) {
            actions(it)
          }
        }
      }
      actionTask("EditorUnSelectWord") {
        "${action(it)} shrinks selection. Try pressing it."
      }
    }
}