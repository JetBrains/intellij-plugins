// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service.takeovermode

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase.checkHighlightingByText
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.service.TypeScriptServiceTestBase.Companion.assertHasServiceItems
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.registry.Registry
import com.intellij.platform.lsp.tests.checkLspHighlighting
import com.intellij.platform.lsp.tests.waitForDiagnosticsFromLspServer
import com.intellij.psi.PsiManager
import com.intellij.util.text.SemVer
import com.intellij.util.xmlb.SettingsInternalApi
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath
import org.jetbrains.vuejs.options.VueSettings
import org.junit.Test
import kotlin.io.path.Path
import kotlin.io.path.readText

class VueLspTypeScriptServiceTest : VueLspServiceTestBase() {

  override fun getBasePath(): String {
    return "${vueRelativeTestDataPath()}/service/volar"
  }

  override fun setUp() {
    super.setUp()
    myFixture.addFileToProject("tsconfig.json", tsconfig)
  }

  @Test
  fun testSimpleVue() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("Simple.vue")
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testVBindShorthand() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_4_0)
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("Simple.vue")
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testEnableSuggestions() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("Simple.vue")
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testDisableSuggestions() {
    val settings = TypeScriptCompilerSettings.getSettings(project)
    settings.showSuggestions = false
    disposeOnTearDown(Disposable { settings.showSuggestions = true })
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("Simple.vue")
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testSimpleRename() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("Simple.vue")
    myFixture.checkLspHighlighting()

    val virtualFileToRename = myFixture.findFileInTempDir("Usage.vue")
                              ?: throw AssertionError("Could not find virtual file to rename")
    val psiFileToRename = PsiManager.getInstance(project).findFile(virtualFileToRename)
                          ?: throw AssertionError("Can't find virtual file to rename")
    myFixture.renameElement(psiFileToRename, "Usage2.vue")

    //no errors
    myFixture.checkLspHighlighting()

    assertCorrectService()
  }

  @Test
  fun testOptionalPropertyInsideObjectLiteralInTSFileCompletion() { // WEB-61886
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.copyDirectoryToProject("${getTestName(true)}/before", ".")
    myFixture.configureFromTempProjectFile("config.ts")

    myFixture.checkLspHighlighting()
    assertCorrectService()

    val elements = myFixture.completeBasic()
    myFixture.type('\n')

    checkHighlightingByText(myFixture, loadAfterText("config.ts"), true)

    val presentationTexts = getPresentationTexts(elements)
    assertTrue("Lookup element presentation must match expected", presentationTexts.contains("base?"))
    assertHasServiceItems(elements, true)
  }

  @Test
  fun testOptionalPropertyInsideQualifiedReferenceInTSFileCompletion() { // WEB-63103
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.copyDirectoryToProject("${getTestName(true)}/before", ".")

    // Volar reports obscuring errors when there's no reference after dot, but we have to test caret placement directly after it
    myFixture.configureFromTempProjectFile("main.ts")

    myFixture.checkLspHighlighting()
    assertCorrectService()

    val elements = myFixture.completeBasic()
    myFixture.type('\t')

    checkHighlightingByText(myFixture, loadAfterText("main.ts"), true)

    val presentationTexts = getPresentationTexts(elements)
    assertTrue("Lookup element presentation must match expected", presentationTexts.contains("bar?"))
    assertHasServiceItems(elements, true)
  }

  @Test
  fun testOptionalParameterPropertyInTSFileCompletion() { // WEB-63103
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.copyDirectoryToProject("${getTestName(true)}/before", ".")

    // Volar reports obscuring errors when there's no reference after dot, but we have to test caret placement directly after it
    myFixture.configureFromTempProjectFile("main.ts")

    myFixture.checkLspHighlighting()
    assertCorrectService()

    val elements = myFixture.completeBasic()
    myFixture.type('\t')

    checkHighlightingByText(myFixture, loadAfterText("main.ts"), true)

    val presentationTexts = getPresentationTexts(elements)
    assertTrue("Lookup element presentation must match expected", presentationTexts.contains("bar?"))
    assertHasServiceItems(elements, true)
  }

  @OptIn(SettingsInternalApi::class)
  @Test
  fun testSimpleCustomVersionVue() {
    myFixture.enableInspections(VueInspectionsProvider())
    val version = SemVer.parseFromText("1.8.10")
    myFixture.configureVueDependencies(
      VueTestModule.VUE_3_0_0,
      additionalDependencies = mapOf("@vue/language-server" to version.toString()),
    )
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    performNpmInstallForPackageJson("package.json")
    val settings = VueSettings.instance(project)
    val state = settings.state
    val old = state.manual.lspServerPackagePath
    disposeOnTearDown(Disposable {
      settings.state = state.copy(
        manual = state.manual.copy(lspServerPackagePath = old)
      )
    })
    val path = myFixture.findFileInTempDir("node_modules/@vue/language-server").path
    assertNotNull(path)
    settings.state = state.copy(
      manual = state.manual.copy(lspServerPackagePath = path)
    )

    myFixture.configureFromTempProjectFile("Simple.vue")
    myFixture.checkLspHighlighting()

    assertCorrectService(version)
  }

  @Test
  fun testMultilineCompletionItem() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    Registry.get("typescript.service.completion.ownContributorsEnabled").setValue(false, testRootDisposable)
    myFixture.copyDirectoryToProject("${getTestName(true)}/before", ".")
    myFixture.configureFromTempProjectFile("main.vue")

    myFixture.checkLspHighlighting()
    myFixture.type("spre")
    myFixture.completeBasic()

    waitForDiagnosticsFromLspServer(project, file.virtualFile)
    checkHighlightingByText(myFixture, loadAfterText("main.vue"), true)
  }

  @Test
  fun testAutoImportActionDoesntBreakTheService() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.copyDirectoryToProject("${getTestName(true)}/before", ".")
    myFixture.configureFromTempProjectFile("main.ts")

    myFixture.checkLspHighlighting()
    myFixture.completeBasic()

    waitForDiagnosticsFromLspServer(project, file.virtualFile)
    checkHighlightingByText(myFixture, loadAfterText("main.ts"), true)
  }

  @Test
  fun testImportsInCreatedFile() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("App.vue")
    myFixture.checkLspHighlighting()
    myFixture.configureFromTempProjectFile("App1.vue")
    myFixture.checkLspHighlighting()
  }

  @Test
  fun testTailwindApplyInterop() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_4_0)
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("Simple.vue")
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  private fun loadAfterText(fileName: String): String =
    Path("$testDataPath/${getTestName(true)}/after/$fileName").readText()

  private fun getPresentationTexts(elements: Array<LookupElement>): List<String?> {
    return elements.map { element ->
      val presentation = LookupElementPresentation()
      element.renderElement(presentation)
      presentation.itemText
    }
  }
}
