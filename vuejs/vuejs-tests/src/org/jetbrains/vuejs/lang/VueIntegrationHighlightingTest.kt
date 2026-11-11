// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.javascript.debugger.NodeJsAppRule
import com.intellij.javascript.nodejs.npm.NpmUtil
import com.intellij.lang.javascript.linter.JSExternalToolFixture.Companion.jsExternalToolFixture
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.polySymbols.html.elements.HtmlElementSymbolDescriptor
import com.intellij.polySymbols.search.PsiLinkedPolySymbol
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.asSafely
import org.jetbrains.vuejs.inspections.VueMissingComponentImportInspection
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class VueIntegrationHighlightingTest {
  private val vue by jsExternalToolFixture(
    JSTempDirFixtureOptions.forBasePath(vueRelativeTestDataPath() + "/highlighting")
      .withMainPackage(NpmUtil.PNPM_PACKAGE_NAME)
      .withNodeVersion(NodeJsAppRule.LATEST_20))

  @Test
  fun testAutoImportVueComponentWithPnpm() {
    copyTestDirectoryAndInstallDependencies()
    vue.fixture.enableInspections(VueMissingComponentImportInspection::class.java, HtmlUnknownTagInspection::class.java)
    vue.fixture.configureFromTempProjectFile("src/HelloWorld.vue")
    vue.fixture.checkHighlighting()
  }

  @Test
  fun testResolveWithJsConfig() {
    copyTestDirectoryAndInstallDependencies()
    vue.fixture.configureFromTempProjectFile("src/HelloWorld.vue")
    // The element lookup and the PSI reads need a read action.
    runReadActionBlocking {
      val tagAtCaret = vue.fixture.elementAtCaret.asSafely<XmlTag>()
      assertNotNull(tagAtCaret)

      val polySymbolDescriptor = tagAtCaret?.descriptor.asSafely<HtmlElementSymbolDescriptor>()
      assertNotNull(polySymbolDescriptor)

      val polySymbol = polySymbolDescriptor?.symbol.asSafely<PsiLinkedPolySymbol>()
      val polySymbolSource = polySymbol?.linkedElement
      assertNotNull(polySymbolSource)

      val sourceFileName = polySymbolSource!!.containingFile.name
      assertTrue(sourceFileName == "index.d.ts", "Wrong source file: $sourceFileName")
    }
  }

  private fun copyTestDirectoryAndInstallDependencies() {
    vue.fixture.copyDirectoryToProject(vue.getTestName(true), "")
    vue.performNpmInstallUsingPackageManager(vue.project, vue.nodePackage, "package.json")
  }
}
