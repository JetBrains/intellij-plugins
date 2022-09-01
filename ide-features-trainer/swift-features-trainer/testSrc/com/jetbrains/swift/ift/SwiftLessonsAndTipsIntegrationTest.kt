package com.jetbrains.swift.ift

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import training.simple.LessonsAndTipsIntegrationTest

@RunWith(JUnit4::class)
internal class SwiftLessonsAndTipsIntegrationTest : LessonsAndTipsIntegrationTest() {
  override val languageId = "Swift"
  override val languageSupport = SwiftSupport()
  override val learningCourse = SwiftLearningCourse()
}