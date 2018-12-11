package training.learn.lesson.kimpl


class LessonSample(val text: String, private val infos: Map<String, SampleInfo>) {
  fun getInfo(tag: String): SampleInfo = infos[tag]!!
}

fun parseLessonSample(text: String): LessonSample {
  var cleanText = text
  // Here could be more general parsing
  val infos = HashMap<String, SampleInfo>()

  val startTag = "<caret>"
  var startIdx = text.indexOf(startTag)

  if (startIdx < 0 ) {
    startIdx = 0
  }
  else {
    cleanText = cleanText.replace(startTag, "")
  }

  infos[START_TAG] = SampleInfo(START_TAG, startIdx)

  return LessonSample(cleanText, infos)
}
