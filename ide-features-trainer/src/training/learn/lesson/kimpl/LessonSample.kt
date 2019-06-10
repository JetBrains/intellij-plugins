package training.learn.lesson.kimpl


data class LessonSample(val text: String,
                        val startOffset: Int = 0,
                        val selection: Pair<Int, Int>? = null)

// Current implementation can be generalized much better
fun parseLessonSample(text: String): LessonSample {
  return trySelectionSample(text)
      ?: tryCaretSample(text)
      ?: LessonSample(text)
}

fun trySelectionSample(text: String): LessonSample? {
  val startSelection = text.indexOf("<select>")
  if (startSelection == -1) return null
  val withoutSelect = text.replace("<select>", "")
  val endSelection = withoutSelect.indexOf("</select>")

  return LessonSample(withoutSelect.replace("</select>", ""),
      startOffset = endSelection, selection = Pair(startSelection ,endSelection))
}

fun tryCaretSample(text: String): LessonSample? {
  val startIdx = text.indexOf("<caret>")
  if (startIdx < 0) return null

  return LessonSample(text.replace("<caret>", ""), startOffset = startIdx)
}
