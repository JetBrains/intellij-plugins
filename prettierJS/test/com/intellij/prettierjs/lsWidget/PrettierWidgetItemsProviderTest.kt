// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.lsWidget

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServicePopupSection
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.prettierjs.PrettierJSTestUtil
import com.intellij.prettierjs.PrettierLanguageService
import com.intellij.prettierjs.PrettierLanguageServiceManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * When several cached Prettier services are ancestors of the current file (nested layout), at most one widget item
 * must be [LanguageServicePopupSection.ForCurrentFile]. Each test copies a layout from `testData/lsWidget/<getTestName>`.
 */
class PrettierWidgetItemsProviderTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = PrettierJSTestUtil.getTestDataPath() + "lsWidget"

  override fun tearDown() {
    try {
      PrettierLanguageServiceManager.getInstance(project).terminateServices()
      // Keep PrettierConfiguration clean for the shared light project, matching PrettierConfigurationTestBase.
      PrettierConfiguration.getInstance(project).state.configurationMode = null
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  /**
   * Copies the test-data layout named after the current test into the project root and returns the package the
   * services are created with. A fake (not installed) package is enough, and we deliberately do not touch
   * [com.intellij.prettierjs.PrettierConfiguration]: services are created with this exact package, and the widget
   * matches using each cached service's own package, so neither path consults the project configuration.
   */
  private fun configureProject(): NodePackage {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    return NodePackage("/fake/node_modules/prettier")
  }

  /**
   * Creates exactly [expectedCount] cached services, one per context file. `package.json` changes asynchronously
   * terminate services, so re-acquire on each event pump until the count stays stable (notifications drained).
   */
  private fun seedServices(nodePackage: NodePackage, expectedCount: Int, vararg contextFiles: VirtualFile) {
    val manager = PrettierLanguageServiceManager.getInstance(project)
    var stableIterations = 0
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

  private fun widgetItemsFor(currentFile: VirtualFile): List<LanguageServiceWidgetItem> =
    PrettierWidgetItemsProvider().createWidgetItems(project, currentFile)

  fun testNestedService() {
    val nodePackage = configureProject()

    // Only the nested package.json declares prettier, so the two files resolve to distinct (root vs nested) services.
    val rootFile = myFixture.findFileInTempDir("index.ts")
    val nestedFile = myFixture.findFileInTempDir("modules/gather-video-common/src/settings.ts")

    seedServices(nodePackage, expectedCount = 2, rootFile, nestedFile)

    val items = widgetItemsFor(nestedFile)
    assertEquals(2, items.size)

    val current = items.filter { it.widgetActionLocation == LanguageServicePopupSection.ForCurrentFile }
    assertEquals("exactly one item should be marked ForCurrentFile", 1, current.size)

    val currentText = current.single().createWidgetAction().templatePresentation.text
    assertTrue("the current-file item should be the nested service, but was: $currentText",
               currentText != null && currentText.contains("gather-video-common"))
  }

  fun testRootService() {
    val nodePackage = configureProject()

    // Only the root package.json declares prettier; a nested file without its own one resolves to the root service.
    val rootFile = myFixture.findFileInTempDir("index.ts")
    val nestedFile = myFixture.findFileInTempDir("modules/sub/src/foo.ts")

    seedServices(nodePackage, expectedCount = 1, rootFile)

    val items = widgetItemsFor(nestedFile)
    assertEquals(1, items.size)
    assertEquals("the root service must be ForCurrentFile",
                 LanguageServicePopupSection.ForCurrentFile, items.single().widgetActionLocation)
  }

  fun testUnsupportedFile() {
    val nodePackage = configureProject()

    val nestedFile = myFixture.findFileInTempDir("modules/gather-video-common/src/settings.ts")
    // A file extension that does not match the default Prettier files pattern.
    val unsupportedFile = myFixture.findFileInTempDir("modules/gather-video-common/notes.txt")

    seedServices(nodePackage, expectedCount = 1, nestedFile)

    val items = widgetItemsFor(unsupportedFile)
    assertTrue("a cached service should still be shown", items.isNotEmpty())
    assertTrue("no item should be ForCurrentFile for an unsupported file",
               items.none { it.widgetActionLocation == LanguageServicePopupSection.ForCurrentFile })
  }
}
