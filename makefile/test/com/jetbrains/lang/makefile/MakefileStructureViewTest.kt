package com.jetbrains.lang.makefile

import com.intellij.testFramework.*
import com.intellij.testFramework.fixtures.*

class MakefileStructureViewTest : BasePlatformTestCase() {
  fun testSimple() {
    val filename = "${getTestName(true)}.mk"
    myFixture.configureByFile("$basePath/$filename")
    myFixture.testStructureView {
      PlatformTestUtil.expandAll(it.tree)
      PlatformTestUtil.assertTreeEqual(it.tree, "-simple.mk\n all\n hello\n world\n")
    }
  }

  override fun getTestDataPath() = BASE_TEST_DATA_PATH
  override fun getBasePath() = "structure"
}