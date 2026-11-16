package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.lang.javascript.JSCodeInsightFixture.Companion.jsCodeInsightFixture
import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.JSTestUtils.checkResolveToDestination
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class NextJsResolveTest {
  private val projectFixture = jsProjectFixture()
  private val js by jsCodeInsightFixture(projectFixture, JSFixtureOptions(NextJsTestUtil.getTestDataPath() + "resolve/", "tsx"))

  @Test
  fun testPagesResolving(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    doTest("declaration", testProjectName = "pagesPathAttributeResolve")
  }

  @Test
  fun testAppResolving(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    doTest("declaration")
  }

  @Test
  fun testGroupResolving(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    doTest("declaration")
  }

  @Test
  fun testGroupResolvingComplex(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    doTest("page.tsx")
  }

  @Test
  fun testSlotResolving(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    doTest("page.tsx")
  }

  @Test
  fun testSlugResolving(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    doTest("[slug]")
  }

  @Test
  fun testJSEmbeddedContentResolving(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    doTest("declaration")
  }

  @Test
  fun testInterceptResolving1(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    doTest("[slug]")
  }

  @Test
  fun testInterceptResolving2(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    doTest("page.tsx")
  }

  @Test
  fun testCatchAllResolving1(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    doTest("[...catchAll]")
  }

  @Test
  fun testCatchAllResolving2(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    doTest("page.tsx")
  }

  @Test
  fun testCatchAllResolving3(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    doTest("page.tsx")
  }

  @Test
  fun testCatchAllResolving4(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    doTest("folderForExactMatching1")
  }

  @Test
  fun testPureReactProject(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    withContext(Dispatchers.EDT) {
      checkResolveToDestination(null, js.getTestName(true), js.fixture, js.getTestName(false), "tsx")
    }
  }

  private suspend fun doTest(destination: String = "", testProjectName: String? = null) {
    withContext(Dispatchers.EDT) {
      checkResolveToDestination(destination, testProjectName ?: "pathAttributeResolve", js.fixture, js.getTestName(false), "tsx")
    }
  }
}