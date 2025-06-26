// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.polySymbols.html.elements.HtmlElementSymbolDescriptor
import com.intellij.javascript.debugger.NodeJsAppRule
import com.intellij.javascript.nodejs.npm.NpmUtil
import com.intellij.lang.javascript.linter.JSExternalToolIntegrationTest
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import org.jetbrains.vuejs.inspections.VueMissingComponentImportInspection

class VueIntegrationHighlightingTest: JSExternalToolIntegrationTest() {

  override fun getBasePath(): String = vueRelativeTestDataPath() + "/highlighting"

  fun testAutoImportVueComponentWithPnpm() {
    copyTestDirectoryAndInstallDependencies()
    myFixture.enableInspections(VueMissingComponentImportInspection::class.java, HtmlUnknownTagInspection::class.java)
    myFixture.configureFromTempProjectFile("src/HelloWorld.vue")
    myFixture.checkHighlighting()
  }

  fun testResolveWithJsConfig() {
    copyTestDirectoryAndInstallDependencies()
    myFixture.configureFromTempProjectFile("src/HelloWorld.vue")
    val tagAtCaret = myFixture.elementAtCaret.asSafely<XmlTag>()
    assertNotNull(tagAtCaret)

    val polySymbolDescriptor = tagAtCaret?.descriptor.asSafely<HtmlElementSymbolDescriptor>()
    assertNotNull(polySymbolDescriptor)

    val polySymbol = polySymbolDescriptor?.symbol.asSafely<PsiSourcedPolySymbol>()
    val polySymbolSource = polySymbol?.source
    assertNotNull(polySymbolSource)

    val sourceFileName = polySymbolSource!!.containingFile.name
    assertTrue("Wrong source file: $sourceFileName", sourceFileName == "index.d.ts")
  }

  private fun copyTestDirectoryAndInstallDependencies() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    performNpmInstallUsingPackageManager(project, nodePackage, "package.json")
  }

  override fun getMainPackageName(): String = NpmUtil.PNPM_PACKAGE_NAME

  override fun configureInterpreterVersion(): NodeJsAppRule = NodeJsAppRule.LATEST_20
}
