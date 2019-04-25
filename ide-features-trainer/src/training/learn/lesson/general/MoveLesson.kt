package training.learn.lesson.general

import com.intellij.diff.tools.util.text.LineOffsetsUtil
import com.intellij.openapi.vcs.ex.compareLines
import com.intellij.util.containers.ContainerUtil
import training.commands.ShowLineNumberCommand
import training.lang.JavaLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample

class MoveLesson(module: Module, lang: String, private val sample: LessonSample) :
    KLesson("Move", module, lang) {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      ShowLineNumberCommand.showLineNumber(editor)

      actionTask("MoveLineDown") {
        "Rearranging lines usually takes two actions: cut and paste. In this IDE, you can do it with just one: " +
            "pressing ${action(it)} will pull down the current line."
      }
      actionTask("MoveLineUp") {
        "Similarly, to pull a line up, use ${action(it)}."
      }
      if (lang == JavaLangSupport.lang) caret(9, 5)
      task("MoveStatementUp") {
        text("Now try moving the whole method up with ${action(it)}.")
        trigger(it, { editor.document.text }, { before, now ->
          checkSwapMoreThan2Lines(before, now)
        })
        test { actions("EditorUp", it) }
      }
    }
}

fun checkSwapMoreThan2Lines(before: String, now: String): Boolean {
  val changes = ContainerUtil.newArrayList(
      compareLines(before, now, LineOffsetsUtil.create(before), LineOffsetsUtil.create(now)
      ).iterateChanges())

  return changes.size > 1
}
