package com.intellij.aws.cloudformation.tests

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.refactoring.rename.RenameProcessor
import com.intellij.testFramework.LightCodeInsightTestCase
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import com.intellij.testFramework.LightPlatformTestCase

class RenameTests : LightCodeInsightTestCase() {
  @Throws(Exception::class)
  fun testSimpleEntity() {
    configureByFile("simpleEntity.template")
    val element = TargetElementUtil.findTargetElement(
        LightPlatformCodeInsightTestCase.myEditor, TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED)
    RenameProcessor(LightPlatformTestCase.getProject(), element, "NEW_NAME", false, false).run()
    checkResultByFile("simpleEntity.after.template")
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("rename")
  }
}
