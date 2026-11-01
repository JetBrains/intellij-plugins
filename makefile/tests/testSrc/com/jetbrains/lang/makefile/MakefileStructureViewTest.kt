package com.jetbrains.lang.makefile

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.StructureViewComponentTestUtil
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@TestApplication
class MakefileStructureViewTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  private lateinit var testName: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, true)
    myFixture.testDataPath = BASE_TEST_DATA_PATH
  }

  @Test
  fun testSimple() {
    val filename = "$testName.mk"
    myFixture.configureByFile("$basePath/$filename")
    StructureViewComponentTestUtil.testStructureView(myFixture) {
      PlatformTestUtil.expandAll(it.tree)
      PlatformTestUtil.assertTreeEqual(it.tree, "-simple.mk\n -all\n -hello\n -world\n")
    }
  }

  private val basePath = "structure"
}