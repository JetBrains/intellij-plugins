// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.lang.typescript.service.TypeScriptServiceFixture.Companion.typeScriptServiceFixture
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.usageView.UsageInfo
import org.jetbrains.astro.getRelativeAstroTestDataPath
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class AstroPluginTypeScriptServiceFindUsagesTest {
  private val astro by typeScriptServiceFixture(
    JSTempDirFixtureOptions.forBasePath(getRelativeAstroTestDataPath() + "/ts-plugin/findUsages").withExtension("astro"))

  @BeforeEach
  fun setUp() {
    // Enable Astro TS find usages bridge for the TS service
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath = TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()
    RegistryManager.getInstance().get("astro.ts.find.usages.enabled").setValue(true, astro.testRootDisposable)
    RegistryManager.getInstance().get("astro.language.server.bundled.enabled").setValue(true, astro.testRootDisposable)
  }

  private fun assertUsagesAt(usages: Collection<UsageInfo>, expectedAbsRanges: Set<Pair<Int, Int>>): Unit = runReadActionBlocking {
    assertTrue(usages.isNotEmpty(), "Expected usages, got ${'$'}{usages.size}")

    val indexUsages = usages.filter { it.file?.name == "index.astro" }
    val actualAbsRanges = mutableSetOf<Pair<Int, Int>>()

    for (usage in indexUsages) {
      val file = usage.file
      val element = usage.element
      val range = usage.rangeInElement

      assertNotNull(file, "Usage file should be index.astro: ${'$'}u")
      assertNotNull(element, "Usage element is null: ${'$'}u")
      assertNotNull(range, "rangeInElement is null for usage: ${'$'}u")

      if (file == null || element == null || range == null) continue

      val absStart = element.textRange.startOffset + range.startOffset
      val absEnd = element.textRange.startOffset + range.endOffset
      actualAbsRanges.add(absStart to absEnd)
    }

    assertTrue(
      actualAbsRanges.containsAll(expectedAbsRanges),
      "Expected ranges $expectedAbsRanges to be present among actual $actualAbsRanges"
    )
  }

  private fun findUsagesWithIndicator(): Collection<UsageInfo> {
    val target = runReadActionBlocking { astro.fixture.elementAtCaret }
    return ProgressManager.getInstance()
      .runProcess(Computable { astro.fixture.findUsages(target) }, EmptyProgressIndicator())
  }

  @Test
  fun testFindUsagesFromTsx() {
    astro.fixture.copyDirectoryToProject(astro.getTestName(false), "")
    astro.fixture.configureFromTempProjectFile("MyComponent.tsx")

    val usages = findUsagesWithIndicator()

    assertUsagesAt(usages, setOf(12 to 23, 56 to 67))
  }

  @Test
  fun testFindUsagesFromJsx() {
    astro.fixture.copyDirectoryToProject(astro.getTestName(false), "")
    astro.fixture.configureFromTempProjectFile("MyJsComponent.jsx")

    val usages = findUsagesWithIndicator()

    assertUsagesAt(usages, setOf(12 to 25, 60 to 73))
  }

  @Test
  fun testFindUsagesFromTsxDefault() {
    astro.fixture.copyDirectoryToProject(astro.getTestName(false), "")
    astro.fixture.configureFromTempProjectFile("MyComponent.tsx")

    val usages = findUsagesWithIndicator()

    assertUsagesAt(usages, setOf(11 to 22, 54 to 65))
  }

  @Test
  fun testFindUsagesFromJsxDefault() {
    astro.fixture.copyDirectoryToProject(astro.getTestName(false), "")
    astro.fixture.configureFromTempProjectFile("MyJsComponent.jsx")

    val usages = findUsagesWithIndicator()

    assertUsagesAt(usages, setOf(11 to 24, 58 to 71))
  }
}
