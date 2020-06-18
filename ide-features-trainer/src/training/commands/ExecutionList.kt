// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.commands

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import org.jdom.Element
import training.learn.interfaces.Lesson
import java.util.*

class ExecutionList(val elements: Queue<Element>,
                    val lesson: Lesson,
                    val project: Project,
                    val documentationMode: Boolean) {
  val editor: Editor
    get() = FileEditorManager.getInstance(project).selectedTextEditor!!
}
