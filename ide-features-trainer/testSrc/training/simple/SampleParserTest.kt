package training.simple

import com.intellij.testFramework.UsefulTestCase
import training.learn.lesson.kimpl.START_TAG
import training.learn.lesson.kimpl.parseLessonSample

class SampleParserTest : UsefulTestCase() {
  fun testStartCaretTag1() {
    val lesson = parseLessonSample("hello <caret>world")
    assertSameLines("hello world", lesson.text)
    assertSame(6, lesson.getInfo(START_TAG).startOffset)
  }

  fun testStartCaretTag2() {
    val lesson = parseLessonSample("<caret>At the start")
    assertSameLines("At the start", lesson.text)
    assertSame(0, lesson.getInfo(START_TAG).startOffset)
  }


  fun testStartCaretTag3() {
    val lesson = parseLessonSample("In the end<caret>")
    assertSameLines("In the end", lesson.text)
    assertSame(10, lesson.getInfo(START_TAG).startOffset)
  }
}
