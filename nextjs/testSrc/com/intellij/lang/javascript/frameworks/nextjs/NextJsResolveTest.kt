package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.lang.javascript.JSTestUtils.checkResolveToDestination
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class NextJsResolveTest : BasePlatformTestCase() {
  override fun getTestDataPath() = NextJsTestUtil.getTestDataPath() + "resolve/"

  fun testPagesResolving() {
    doTest("declaration", testProjectName = "pagesPathAttributeResolve")
  }

  fun testAppResolving() {
    doTest("declaration")
  }

  fun testGroupResolving() {
    doTest("declaration")
  }

  fun testGroupResolvingComplex() {
    doTest("page.tsx")
  }

  fun testSlotResolving() {
    doTest("page.tsx")
  }

  fun testSlugResolving() {
    doTest("[slug]")
  }

  fun testJSEmbeddedContentResolving() {
    doTest("declaration")
  }

  fun testInterceptResolving1() {
    doTest("[slug]")
  }

  fun testInterceptResolving2() {
    doTest("page.tsx")
  }

  fun testCatchAllResolving1() {
    doTest("[...catchAll]")
  }

  fun testCatchAllResolving2() {
    doTest("page.tsx")
  }

  fun testCatchAllResolving3() {
    doTest("page.tsx")
  }

  fun testCatchAllResolving4() {
    doTest("folderForExactMatching1")
  }

  fun testPureReactProject() {
    checkResolveToDestination(null, getTestName(true), myFixture, getTestName(false), "tsx")
  }

  private fun doTest(destination: String = "", testProjectName: String? = null) {
    checkResolveToDestination(destination, testProjectName ?: "pathAttributeResolve", myFixture, getTestName(false), "tsx")
  }
}