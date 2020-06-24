// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.commands.kotlin

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.IdeFocusManager
import training.learn.lesson.kimpl.KLesson
import java.awt.Component

open class TaskRuntimeContext(val lesson: KLesson, val editor: Editor, val project: Project, val disposable: Disposable, private val previousGetter: () -> PreviousTaskInfo) {
  constructor(base: TaskRuntimeContext) : this(base.lesson, base.editor, base.project, base.disposable, base.previousGetter)

  val focusOwner: Component?
    get() = IdeFocusManager.getInstance(project).focusOwner

  val previous: PreviousTaskInfo
    get() = previousGetter()
}