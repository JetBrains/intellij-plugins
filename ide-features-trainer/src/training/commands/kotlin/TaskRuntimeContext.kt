// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.commands.kotlin

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.IdeFocusManager
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonExecutor
import training.learn.lesson.kimpl.LessonSample
import java.awt.Component

open class TaskRuntimeContext(private val lessonExecutor: LessonExecutor,
                              val taskDisposable: Disposable,
                              val restorePreviousTaskCallback: () -> Unit,
                              private val previousGetter: () -> PreviousTaskInfo) {
  constructor(base: TaskRuntimeContext)
    : this(base.lessonExecutor, base.taskDisposable, base.restorePreviousTaskCallback, base.previousGetter)

  val editor: Editor get() = lessonExecutor.editor
  val project: Project get() = lessonExecutor.project
  val lessonDisposable: Disposable get() = lessonExecutor

  val focusOwner: Component?
    get() = IdeFocusManager.getInstance(project).focusOwner

  val previous: PreviousTaskInfo
    get() = previousGetter()

  val virtualFile: VirtualFile
    get() = FileDocumentManager.getInstance().getFile(editor.document) ?: error("No virtual file for ${editor.document}")

  fun setSample(sample: LessonSample) = lessonExecutor.setSample(sample)
}