package com.jetbrains.lang.makefile

import com.intellij.testFramework.fixtures.*

class MakefileVariableRenameTest : BasePlatformTestCase() {
  fun testVarFromDeclaration() = doTest("NEW_NAME")
  fun testVarFromUsageInRecipe() = doTest("NEW_NAME")
  fun testVarUsedInPrerequisite() = doTest("NEW_NAME")
  fun testVarUsedInTarget() = doTest("NEW_NAME")
  fun testVarSimpleAssignmentTypes() = doTest("NEW_NAME")

  private fun doTest(newName: String) {
    myFixture.configureByFile("$basePath/${getTestName(true)}.mk")
    myFixture.renameElementAtCaret(newName)
    myFixture.checkResultByFile("$basePath/${getTestName(true)}.gold.mk")
  }

  override fun getTestDataPath() = BASE_TEST_DATA_PATH
  override fun getBasePath() = "rename/variables"
}
