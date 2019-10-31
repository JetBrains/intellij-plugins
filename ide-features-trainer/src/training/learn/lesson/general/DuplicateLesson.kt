/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.lesson.general

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample

class DuplicateLesson(module: Module, lang: String, private val sample: LessonSample) :
    KLesson("Duplicate", module, lang) {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)

      actionTask("EditorDuplicate") { "Duplicate any line with ${action(it)}." }

      task("EditorDuplicate") {
        text("You can do the same thing with multiple lines, too. Simply select two or more lines and duplicate them with ${action(it)}.")
        trigger(it, {
          val selection = editor.selectionModel
          val start = selection.selectionStartPosition?.line ?: 0
          val end = selection.selectionEndPosition?.line ?: 0
          end - start
        }, { _, new -> new >= 2 })
        test { actions("EditorUp", "EditorLineStart", "EditorDownWithSelection", "EditorDownWithSelection", it) }
      }
    }
}