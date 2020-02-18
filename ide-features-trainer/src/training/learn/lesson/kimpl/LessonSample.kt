// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import com.intellij.openapi.editor.Editor

data class LessonSample(val text: String,
                        val startOffset: Int = 0,
                        val selection: Pair<Int, Int>? = null)

// Current implementation can be generalized much better
fun parseLessonSample(text: String): LessonSample {
  return trySelectionSample(text)
         ?: tryCaretSample(text)
         ?: LessonSample(text)
}

private fun trySelectionSample(text: String): LessonSample? {
  val startSelection = text.indexOf("<select>")
  if (startSelection == -1) return null
  val withoutSelect = text.replace("<select>", "")
  val endSelection = withoutSelect.indexOf("</select>")

  return LessonSample(withoutSelect.replace("</select>", ""),
                      startOffset = endSelection, selection = Pair(startSelection, endSelection))
}

private fun tryCaretSample(text: String): LessonSample? {
  val startIdx = text.indexOf("<caret>")
  if (startIdx < 0) return null

  return LessonSample(text.replace("<caret>", ""), startOffset = startIdx)
}

fun prepareSampleFromCurrentState(editor: Editor): LessonSample {
  val text = editor.document.text
  val currentCaret = editor.caretModel.currentCaret
  if (currentCaret.hasSelection()) {
    return LessonSample(text, startOffset = currentCaret.offset, selection = Pair(currentCaret.selectionStart, currentCaret.selectionEnd))
  }
  else {
    return LessonSample(text, startOffset = currentCaret.offset)
  }
}