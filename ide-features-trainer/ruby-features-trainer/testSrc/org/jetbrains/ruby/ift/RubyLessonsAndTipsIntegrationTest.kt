package org.jetbrains.ruby.ift

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import training.simple.LessonsAndTipsIntegrationTest

@RunWith(JUnit4::class)
internal class RubyLessonsAndTipsIntegrationTest : LessonsAndTipsIntegrationTest() {
  override val languageId = "ruby"
  override val languageSupport = RubyLangSupport()
  override val learningCourse = RubyLearningCourse()
}