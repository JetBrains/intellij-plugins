package training.simple

import com.intellij.testFramework.UsefulTestCase
import training.learn.lesson.kimpl.parseLessonSample

class SampleParserTest : UsefulTestCase() {
  fun testStartCaretTag1() {
    val sample = parseLessonSample("hello <caret>world")
    assertSameLines("hello world", sample.text)
    assertSame(6, sample.startOffset)
  }

  fun testStartCaretTag2() {
    val sample = parseLessonSample("<caret>At the start")
    assertSameLines("At the start", sample.text)
    assertSame(0, sample.startOffset)
  }

  fun testStartCaretTag3() {
    val sample = parseLessonSample("In the end<caret>")
    assertSameLines("In the end", sample.text)
    assertSame(10, sample.startOffset)
  }

  fun testSelectTag() {
    val sample = parseLessonSample("""
      hello <select>world
      next </select>line
    """.trimIndent())
    assertSameLines("""
      hello world
      next line
    """.trimIndent(), sample.text)
    assertNotNull(sample.selection)
    assertSame(17, sample.startOffset)
    assertSame(6, sample.selection?.first ?: -1)
    assertSame(17, sample.selection?.second ?: -1)
  }
}
