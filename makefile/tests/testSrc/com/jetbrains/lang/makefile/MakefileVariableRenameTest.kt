package com.jetbrains.lang.makefile

import com.intellij.openapi.application.EDT
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/makefile/tests/testData")
class MakefileVariableRenameTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  private lateinit var testName: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, true)
  }

  @Test
  fun testVarFromDeclaration(): Unit = doTest("NEW_NAME")
  @Test
  fun testVarFromUsageInRecipe(): Unit = doTest("NEW_NAME")
  @Test
  fun testVarUsedInPrerequisite(): Unit = doTest("NEW_NAME")
  @Test
  fun testVarUsedInTarget(): Unit = doTest("NEW_NAME")
  @Test
  fun testVarSimpleAssignmentTypes(): Unit = doTest("NEW_NAME")

  private fun doTest(newName: String): Unit = timeoutRunBlocking {
    myFixture.configureByFile("$basePath/$testName.mk")
    withContext(Dispatchers.EDT) {
      myFixture.renameElementAtCaret(newName)
      myFixture.checkResultByFile("$basePath/$testName.gold.mk")
    }
  }

  private val basePath = "rename/variables"
}
