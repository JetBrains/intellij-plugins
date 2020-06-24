// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.general

import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskRuntimeContext
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

      task("SurroundWith") {
        proposeIfModified {
          editor.caretModel.currentCaret.selectionStart != previous.sample.selection?.first ||
          editor.caretModel.currentCaret.selectionEnd != previous.sample.selection?.second
        }
        text("Press ${action(it)} to surround selected code with some template code.")
        triggerByListItemAndHighlight { item ->
          surroundItems.all { need -> wordIsPresent(item.toString(), need) }
        }
        test { actions(it) }
      }

      task {
        text("Choose <code>$surroundItemName</code> item.")
        stateCheck {
          editor.document.charsSequence.let { sequence ->
            surroundItems.all { sequence.contains(it) }
          }
        }
        restoreByUi()
        test {
          type("${surroundItems.joinToString(separator = " ")}\n") }
      }

      prepareRuntimeTask {
        editor.caretModel.currentCaret.moveCaretRelatively(0, lineShiftBeforeUnwrap, false, true)
      }
      prepareRuntimeTask { // restore point
        prepareSample(previous.sample)
      }

      task("Unwrap") {
        proposeIfModified {
          editor.caretModel.currentCaret.logicalPosition.line != previous.position.line
        }
        text("Lets return to the initial file with unwrapping action by ${action(it)}.")
        triggerByListItemAndHighlight { item ->
          wordIsPresent(item.toString(), surroundItems[0])
        }
        test { actions(it) }
      }
      task {
        restoreByUi()
        text("Choose <code>Unwrap ${surroundItems[0]}</code> item.")
        stateCheck {
          editor.document.charsSequence.let { sequence ->
            !surroundItems.any { sequence.contains(it) }
          }
        }
        test { type("${surroundItems[0]}\n") }
      }
    }

  private fun wordIsPresent(text: String, word: String): Boolean {
    var index = 0
    while(index != -1 && index < text.length) {
      index = text.indexOf(word, startIndex = index)
      if (index != -1) {
        if ((index == 0 || !text[index - 1].isLetterOrDigit()) &&
            (index + word.length == text.length || !text[index + word.length + 1].isLetterOrDigit()))
          return true
        index = index + word.length
      }
    }
    return false
  }

  private fun TaskContext.proposeIfModified(checkCaret: TaskRuntimeContext.() -> Boolean) {
    proposeRestore {
      when {
        editor.document.text != previous.text -> TaskContext.RestoreProposal.Modification
        checkCaret() -> TaskContext.RestoreProposal.Caret
        else -> TaskContext.RestoreProposal.None
      }
    }
  }
}