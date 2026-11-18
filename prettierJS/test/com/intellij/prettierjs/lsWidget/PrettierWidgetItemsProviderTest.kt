// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.lsWidget

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServicePopupSection
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.prettierjs.PrettierJSTestUtil
import com.intellij.prettierjs.PrettierLanguageService
import com.intellij.prettierjs.PrettierLanguageServiceManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.testNameFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

/**
 * When several cached Prettier services are ancestors of the current file (nested layout), at most one widget item
 * must be [LanguageServicePopupSection.ForCurrentFile]. Each test copies a layout from `testData/lsWidget/<getTestName>`.
 */
@TestApplication
class PrettierWidgetItemsProviderTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)
  private val project get() = myFixture.project
  private val testName by testNameFixture()

  @BeforeEach
  fun setUp() {
    myFixture.testDataPath = PrettierJSTestUtil.getTestDataPath() + "lsWidget"
  }

  @AfterEach
  fun tearDown() {
    PrettierLanguageServiceManager.getInstance(project).terminateServices()
  }

  /**
   * Copies the test-data layout named after the current test into the project root and returns the package the
   * services are created with. A fake (not installed) package is enough, and we deliberately do not touch
   * [com.intellij.prettierjs.PrettierConfiguration]: services are created with this exact package, and the widget
   * matches using each cached service's own package, so neither path consults the project configuration.
   */
  private fun configureProject(): NodePackage {
    myFixture.copyDirectoryToProject(testName, "")
    return NodePackage("/fake/node_modules/prettier")
  }

  /**
   * Creates exactly [expectedCount] cached services, one per context file. `package.json` changes asynchronously
   * terminate services, so re-acquire on each event pump until the count stays stable (notifications drained).
   */
  private fun seedServices(nodePackage: NodePackage, expectedCount: Int, vararg contextFiles: VirtualFile) {
    val manager = PrettierLanguageServiceManager.getInstance(project)
    var stableIterations = 0
    timeoutRunBlocking(30.seconds) {
      withContext(Dispatchers.EDT) {
        PlatformTestUtil.waitWithEventsDispatching(
          "Expected $expectedCount cached Prettier service(s)",
          {
            for (contextFile in contextFiles) {
              PrettierLanguageService.getInstance(project, contextFile, nodePackage)
            }
            if (manager.jsLinterServices.size == expectedCount) stableIterations++ else stableIterations = 0
            stableIterations >= 3
          },
          20,
        )
      }
    }
  }

  private fun widgetItemsFor(currentFile: VirtualFile): List<LanguageServiceWidgetItem> =
    runReadActionBlocking { PrettierWidgetItemsProvider().createWidgetItems(project, currentFile) }

  @Test
  fun testNestedService() {
    val nodePackage = configureProject()

    // Only the nested package.json declares prettier, so the two files resolve to distinct (root vs nested) services.
    val rootFile = myFixture.findFileInTempDir("index.ts")
    val nestedFile = myFixture.findFileInTempDir("modules/gather-video-common/src/settings.ts")

    seedServices(nodePackage, expectedCount = 2, rootFile, nestedFile)

    val items = widgetItemsFor(nestedFile)
    assertEquals(2, items.size)

    val current = items.filter { it.widgetActionLocation == LanguageServicePopupSection.ForCurrentFile }
    assertEquals(1, current.size, "exactly one item should be marked ForCurrentFile")

    val currentText = current.single().createWidgetAction().templatePresentation.text
    assertTrue(currentText != null && currentText.contains("gather-video-common"),
               "the current-file item should be the nested service, but was: $currentText")
  }

  @Test
  fun testRootService() {
    val nodePackage = configureProject()

    // Only the root package.json declares prettier; a nested file without its own one resolves to the root service.
    val rootFile = myFixture.findFileInTempDir("index.ts")
    val nestedFile = myFixture.findFileInTempDir("modules/sub/src/foo.ts")

    seedServices(nodePackage, expectedCount = 1, rootFile)

    val items = widgetItemsFor(nestedFile)
    assertEquals(1, items.size)
    assertEquals(LanguageServicePopupSection.ForCurrentFile, items.single().widgetActionLocation,
                 "the root service must be ForCurrentFile")
  }

  @Test
  fun testUnsupportedFile() {
    val nodePackage = configureProject()

    val nestedFile = myFixture.findFileInTempDir("modules/gather-video-common/src/settings.ts")
    // A file extension that does not match the default Prettier files pattern.
    val unsupportedFile = myFixture.findFileInTempDir("modules/gather-video-common/notes.txt")

    seedServices(nodePackage, expectedCount = 1, nestedFile)

    val items = widgetItemsFor(unsupportedFile)
    assertTrue(items.isNotEmpty(), "a cached service should still be shown")
    assertTrue(items.none { it.widgetActionLocation == LanguageServicePopupSection.ForCurrentFile },
               "no item should be ForCurrentFile for an unsupported file")
  }
}
