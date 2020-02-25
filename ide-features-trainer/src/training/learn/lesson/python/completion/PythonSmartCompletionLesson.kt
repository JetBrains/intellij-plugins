// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.completion

import training.commands.kotlin.TaskContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonUtil
import training.learn.lesson.kimpl.parseLessonSample

class PythonSmartCompletionLesson(module: Module) : KLesson("Smart Completion", module, "Python") {
  private val sample = parseLessonSample("""
    def f(x, file):
      x.append(file)
      x.rem<caret>
  """.trimIndent())

  override val lessonContent: LessonContext.() -> Unit
    get() {
      val methodName = "remove_duplicates"
      val insertedCode = "ove_duplicates()"
      return {
        prepareSample(sample)
        actionTask("CodeCompletion") {
          proposeRestoreMe()
          "Try to use Basic Completion by pressing ${action(it)}."
        }
        task("SmartTypeCompletion") {
          text("Unfortunately IDE has no direct information about ${code("x")} type. " +
               "But sometimes it can guess completion by the context! Use ${action(it)} to invoke Smart Completion.")
          triggerByListItemAndHighlight { ui ->
            ui.toString().contains(methodName)
          }
          proposeRestoreMe()
          test { actions(it) }
        }
        task {
          val result = LessonUtil.insertIntoSample(sample, insertedCode)
          text("Now just choose ${code(methodName)} item to finish this lesson.")
          restoreByUi()
          stateCheck {
            editor.document.text == result
          }
          test {
            ideFrame {
              jListContains(methodName).item(methodName).doubleClick()
            }
          }
        }
      }
    }

  private fun TaskContext.proposeRestoreMe() {
    proposeRestore {
      LessonUtil.checkExpectedStateOfEditor(editor, sample) { change ->
        change.isEmpty()
      }
    }
  }
}