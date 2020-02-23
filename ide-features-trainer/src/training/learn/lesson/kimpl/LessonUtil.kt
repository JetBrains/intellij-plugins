// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import com.intellij.openapi.editor.Editor
import training.commands.kotlin.TaskContext

object LessonUtil {
  fun checkExpectedStateOfEditor(editor: Editor,
                                 sample: LessonSample,
                                 checkModification: (String) -> Boolean): TaskContext.RestoreProposal {
    val prefix = sample.text.substring(0, sample.startOffset)
    val postfix = sample.text.substring(sample.startOffset)

    val docText = editor.document.charsSequence
    return if (docText.startsWith(prefix) && docText.endsWith(postfix)) {
      val middle = docText.subSequence(prefix.length, docText.length - postfix.length).toString()
      if (checkModification(middle)) {
        val offset = editor.caretModel.offset
        if (prefix.length <= offset && offset <= prefix.length + middle.length) {
          TaskContext.RestoreProposal.None
        }
        else {
          TaskContext.RestoreProposal.Caret
        }
      }
      else {
        TaskContext.RestoreProposal.Modification
      }
    }
    else {
      TaskContext.RestoreProposal.Modification
    }
  }
}