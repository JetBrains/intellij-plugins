// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.general

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample

abstract class SurroundAndUnwrapLesson(module: Module, lang: String) :
  KLesson("Surround and Unwrap", module, lang) {

  protected abstract val sample: LessonSample

  protected abstract val surroundItems: Array<String>
  protected abstract val lineShiftBeforeUnwrap: Int

  protected open val surroundItemName: String
    get() = surroundItems.joinToString(separator = "/")

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)

      actionTask("SurroundWith") {
        "Press ${action(it)} to surround selected code with some template code."
      }

      task {
        text("Choose <code>$surroundItemName</code> item.")
        stateCheck {
          editor.document.charsSequence.let { sequence ->
            surroundItems.all { sequence.contains(it) }
          }
        }
        test {
          type("${surroundItems.joinToString(separator = " ")}\n") }
      }
      task("Unwrap") {
        editor.caretModel.currentCaret.moveCaretRelatively(0, lineShiftBeforeUnwrap, false, true)
        text("Lets return to the initial file with unwrapping action by ${action(it)}.")
        trigger(it)
        test { actions(it) }
      }
      task {
        text("Choose <code>Unwrap ${surroundItems[0]}</code> item.")
        stateCheck {
          editor.document.charsSequence.let { sequence ->
            !surroundItems.any { sequence.contains(it) }
          }
        }
        test { type("${surroundItems[0]}\n") }
      }
    }
}