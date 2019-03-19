package training.learn.lesson.general

import com.intellij.diff.tools.util.text.LineOffsetsUtil
import com.intellij.openapi.vcs.ex.compareLines
import com.intellij.util.containers.ContainerUtil
import training.commands.ShowLineNumberCommand
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
        "Similarly, to pull a line up, use ${action(it)}"
      }
      task("MoveStatementUp") {
        text("Now try moving the whole method up with ${action(it)}")
        trigger(it, { editor.document.text }, { before, now ->
          val changes = ContainerUtil.newArrayList(
              compareLines(before, now, LineOffsetsUtil.create(before), LineOffsetsUtil.create(now)
              ).iterateChanges())

          // Check, that more than 2 lines where changed
          !changes.isEmpty() &&
              (changes[0].end1 - changes[0].start1 > 1 || changes[0].end2 - changes[0].start2 > 1)
        })
        test { actions("EditorUp", it) }
      }
    }
}