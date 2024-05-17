package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.JSTestUtils.checkResolveToDestination
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroPathAttributeResolveTest : AstroCodeInsightTestCase("codeInsight/resolve") {

  fun testAHrefIndexResolve() {
    doPathResolveTest("index.astro")
  }

  fun testCustomLinkHrefDirectoryResolve() {
    doPathResolveTest("insideDeclaration")
  }

  fun testLinkHrefFileResolve() {
    doPathResolveTest("component.astro")
  }

  fun testAHrefFileWithExtensionResolve() {
    doPathResolveTest("component.astro")
  }

  fun testLinkHrefNotPagesResolve() {
    doPathResolveTest("index.astro")
  }

  fun testLinkHrefUnresolvedDirectoryResolve() {
    doPathResolveTest()
  }

  fun testRoutePathImportNotResolve() {
    doPathResolveTest()
  }

  fun testPathImportResolve() {
    doPathResolveTest("component.astro")
  }

  private fun doPathResolveTest(destination: String? = null) {
    checkResolveToDestination(destination, myFixture, getTestName(false), "astro")
  }
}