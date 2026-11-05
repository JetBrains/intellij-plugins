package org.jetbrains.ruby.ift

import com.intellij.testFramework.junit5.TestApplication
import training.simple.LessonsAndTipsIntegrationTest

@TestApplication
internal class RubyLessonsAndTipsIntegrationTest : LessonsAndTipsIntegrationTest() {
  override val languageId = "ruby"
  override val languageSupport = RubyLangSupport()
  override val learningCourse = RubyLearningCourse()
}