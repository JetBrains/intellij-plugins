package com.jetbrains.lang.makefile

import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.StructureViewComponentTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MakefileStructureViewTest : BasePlatformTestCase() {
  fun testSimple() {
    val filename = "${getTestName(true)}.mk"
    myFixture.configureByFile("$basePath/$filename")
    StructureViewComponentTestUtil.testStructureView(myFixture) {
      PlatformTestUtil.expandAll(it.tree)
      PlatformTestUtil.assertTreeEqual(it.tree, "-simple.mk\n -all\n -hello\n -world\n")
    }
  }

  override fun getTestDataPath() = BASE_TEST_DATA_PATH
  override fun getBasePath() = "structure"
}