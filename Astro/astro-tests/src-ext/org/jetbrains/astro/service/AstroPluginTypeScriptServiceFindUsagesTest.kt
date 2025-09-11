// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.typescript.compiler.TypeScriptServiceHolder
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.lang.typescript.service.TypeScriptServiceTestBase
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.usageView.UsageInfo
import org.jetbrains.astro.getRelativeAstroTestDataPath

class AstroPluginTypeScriptServiceFindUsagesTest : TypeScriptServiceTestBase() {
  override fun getExtension(): String = "astro"

  override fun getBasePath(): String = getRelativeAstroTestDataPath() + "/ts-plugin/findUsages"


  override val service: TypeScriptServerServiceImpl
    get() {
      return TypeScriptServiceHolder.getForFile(project, file.virtualFile) as TypeScriptServerServiceImpl
    }


  override fun setUp() {
    super.setUp()
    // Enable Astro TS find usages bridge for the TS service
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath = TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()
    RegistryManager.getInstance().get("astro.ts.find.usages.enabled").setValue(true, testRootDisposable)
    RegistryManager.getInstance().get("astro.language.server.bundled.enabled").setValue(true, testRootDisposable)
  }

  private fun assertUsagesAt(usages: Collection<UsageInfo>, expectedAbsRanges: Set<Pair<Int, Int>>) {
    assertTrue("Expected usages, got ${'$'}{usages.size}", usages.isNotEmpty())

    val indexUsages = usages.filter { it.file?.name == "index.astro" }
    val actualAbsRanges = mutableSetOf<Pair<Int, Int>>()

    for (usage in indexUsages) {
      val file = usage.file
      val element = usage.element
      val range = usage.rangeInElement

      assertNotNull("Usage file should be index.astro: ${'$'}u", file)
      assertNotNull("Usage element is null: ${'$'}u", element)
      assertNotNull("rangeInElement is null for usage: ${'$'}u", range)

      if (file == null || element == null || range == null) continue

      val absStart = element.textRange.startOffset + range.startOffset
      val absEnd = element.textRange.startOffset + range.endOffset
      actualAbsRanges.add(absStart to absEnd)
    }

    assertTrue(
      "Expected ranges $expectedAbsRanges to be present among actual $actualAbsRanges",
      actualAbsRanges.containsAll(expectedAbsRanges)
    )
  }

  private fun findUsagesWithIndicator(): Collection<UsageInfo> =
    ProgressManager.getInstance()
      .runProcess(Computable { myFixture.findUsages(myFixture.elementAtCaret) }, EmptyProgressIndicator())

  fun testFindUsagesFromTsx() {
    myFixture.copyDirectoryToProject(getTestName(false), "")
    myFixture.configureFromTempProjectFile("MyComponent.tsx")

    val usages = findUsagesWithIndicator()

    assertUsagesAt(usages, setOf(12 to 23, 56 to 67))
  }

  fun testFindUsagesFromJsx() {
    myFixture.copyDirectoryToProject(getTestName(false), "")
    myFixture.configureFromTempProjectFile("MyJsComponent.jsx")

    val usages = findUsagesWithIndicator()

    assertUsagesAt(usages, setOf(12 to 25, 60 to 73))
  }

  fun testFindUsagesFromTsxDefault() {
    myFixture.copyDirectoryToProject(getTestName(false), "")
    myFixture.configureFromTempProjectFile("MyComponent.tsx")

    val usages = findUsagesWithIndicator()

    assertUsagesAt(usages, setOf(11 to 22, 54 to 65))
  }

  fun testFindUsagesFromJsxDefault() {
    myFixture.copyDirectoryToProject(getTestName(false), "")
    myFixture.configureFromTempProjectFile("MyJsComponent.jsx")

    val usages = findUsagesWithIndicator()

    assertUsagesAt(usages, setOf(11 to 24, 58 to 71))
  }
}
