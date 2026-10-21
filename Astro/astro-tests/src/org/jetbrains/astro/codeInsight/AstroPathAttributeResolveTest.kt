package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.JSTestUtils.checkResolveToDestination
import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.astro.AstroFixture
import org.jetbrains.astro.astroFixture
import org.junit.jupiter.api.Test

@TestApplication
class AstroPathAttributeResolveTest {

  private val astro: AstroFixture by astroFixture("codeInsight/resolve")

  @Test
  fun testAHrefIndexResolve(): Unit = timeoutRunBlocking {
    doPathResolveTest("index.astro")
  }

  @Test
  fun testCustomLinkHrefDirectoryResolve(): Unit = timeoutRunBlocking {
    doPathResolveTest("insideDeclaration")
  }

  @Test
  fun testLinkHrefFileResolve(): Unit = timeoutRunBlocking {
    doPathResolveTest("component.astro")
  }

  @Test
  fun testAHrefFileWithExtensionResolve(): Unit = timeoutRunBlocking {
    doPathResolveTest("component.astro")
  }

  @Test
  fun testLinkHrefNotPagesResolve(): Unit = timeoutRunBlocking {
    doPathResolveTest("index.astro")
  }

  @Test
  fun testLinkHrefUnresolvedDirectoryResolve(): Unit = timeoutRunBlocking {
    doPathResolveTest()
  }

  @Test
  fun testRoutePathImportNotResolve(): Unit = timeoutRunBlocking {
    doPathResolveTest()
  }

  @Test
  fun testPathImportResolve(): Unit = timeoutRunBlocking {
    doPathResolveTest("component.astro")
  }

  private suspend fun doPathResolveTest(destination: String? = null) {
    // The helper copies the files and resolves the reference at the caret without a read action.
    withContext(Dispatchers.EDT) {
      checkResolveToDestination(destination, astro.fixture, astro.getTestName(false), "astro")
    }
  }
}