package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.lang.javascript.JSTestUtils.checkResolveToDestination
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class NextJsResolveTest : BasePlatformTestCase() {
  override fun getTestDataPath() = NextJsTestUtil.getTestDataPath() + "resolve/"

  fun testPagesResolving() {
    doTest("declaration", "pagesPathAttributeResolve")
  }

  fun testAppResolving() {
    doTest("declaration")
  }

  fun testGroupResolving() {
    doTest("index.tsx")
  }

  fun testSlotResolving() {
    doTest("page.tsx")
  }

  fun testSlugResolving() {
    doTest("[slug]")
  }

  private fun doTest(destination: String = "", testProjectName: String? = null) {
    checkResolveToDestination(destination, testProjectName ?: "pathAttributeResolve", myFixture, getTestName(false), "tsx")
  }
}