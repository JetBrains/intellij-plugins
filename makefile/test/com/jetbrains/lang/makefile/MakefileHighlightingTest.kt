package com.jetbrains.lang.makefile

import com.intellij.testFramework.fixtures.*

class MakefileHighlightingTest : BasePlatformTestCase() {
  fun testUnresolved() = doTest()
  fun testMultiunresolved() = doTest()
  fun testTargetspecificvars() = doTest()

  fun doTest(checkInfos: Boolean = false) { myFixture.testHighlighting(true, checkInfos, true, "$basePath/${getTestName(true)}.mk") }

  override fun getTestDataPath() = BASE_TEST_DATA_PATH
  override fun getBasePath() = "highlighting"
}