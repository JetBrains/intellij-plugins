package com.jetbrains.lang.makefile

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.find.FindManager
import com.intellij.find.impl.FindManagerImpl
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/makefile/tests/testData")
class MakefileFindUsagesTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  private lateinit var testName: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, true)
  }

  @Test
  fun testSimple() {
    myFixture.configureByFile("$basePath/$testName.mk")
    val usages = runReadActionBlocking { myFixture.testFindUsages() }

    assertEquals(2, usages.size)
  }

  @Test
  fun testPhony(): Unit = notSearchableForUsages()
  @Test
  fun testForce(): Unit = notSearchableForUsages()

  fun notSearchableForUsages() {
    myFixture.configureByFiles("$basePath/$testName.mk")
    val handler = runReadActionBlocking {
      val targetElement = TargetElementUtil.findTargetElement(myFixture.editor, TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED)
      (FindManager.getInstance(myFixture.project) as FindManagerImpl).findUsagesManager.getFindUsagesHandler(targetElement!!, false)
    }

    assertNull(handler)
  }

  private val basePath = "findUsages"
}