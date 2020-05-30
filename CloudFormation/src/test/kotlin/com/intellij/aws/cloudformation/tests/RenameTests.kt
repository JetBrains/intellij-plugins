package com.intellij.aws.cloudformation.tests

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.refactoring.rename.RenameProcessor
import com.intellij.testFramework.LightPlatformCodeInsightTestCase

class RenameTests : LightPlatformCodeInsightTestCase() {
  fun testSimpleEntity() {
    configureByFile("simpleEntity.template")
    val element = TargetElementUtil.findTargetElement(
        editor, TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED)!!
    RenameProcessor(getProject(), element, "NEW_NAME", false, false).run()
    checkResultByFile("simpleEntity.after.template")
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("rename")
  }
}
