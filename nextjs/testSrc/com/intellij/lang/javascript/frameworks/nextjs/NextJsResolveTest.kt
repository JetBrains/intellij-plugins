package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.lang.javascript.JSTestUtils.checkResolveToDestination
import com.intellij.psi.PsiNamedElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.asSafely

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
    doTest("[...slug]")
  }

  fun testInterceptResolving2() {
    doTest("page.tsx")
  }

  private fun doTest(destination: String = "", testProjectName: String? = null) {
    checkResolveToDestination(destination, testProjectName ?: "pathAttributeResolve", myFixture, getTestName(false), "tsx")
  }
}