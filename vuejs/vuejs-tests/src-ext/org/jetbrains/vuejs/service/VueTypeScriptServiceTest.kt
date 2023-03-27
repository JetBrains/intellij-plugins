// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.service

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase.checkHighlightByFile
import com.intellij.lang.javascript.service.JSLanguageService
import com.intellij.lang.javascript.service.JSLanguageServiceBase
import com.intellij.lang.javascript.service.JSLanguageServiceProvider
import com.intellij.lang.javascript.typescript.service.TypeScriptServiceTestBase
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT
import com.intellij.openapi.actionSystem.CommonDataKeys.PSI_ELEMENT
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.refactoring.actions.RenameElementAction
import com.intellij.refactoring.rename.PsiElementRenameHandler.DEFAULT_NAME
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.UIUtil
import com.intellij.webSymbols.moveToOffsetBySignature
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.typescript.service.VueTypeScriptService
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath
import org.junit.Test

class VueTypeScriptServiceTest : TypeScriptServiceTestBase() {
  override fun getService(): JSLanguageServiceBase =
    JSLanguageServiceProvider.getLanguageServices(project)
      .firstNotNullOf { it as? VueTypeScriptService }

  override fun getExtension(): String {
    return "vue"
  }

  override fun getBasePath(): String {
    return vueRelativeTestDataPath() + BASE_PATH
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testSimpleVue() {
    doTestWithCopyDirectory()
    myFixture.configureByFile("SimpleVueNoTs.vue")
    checkHighlightingByOptions(false)
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testGotoDeclaration() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10)
    myFixture.configureByFile("GotoDeclaration.vue")
    myFixture.performEditorAction("GotoDeclaration")
    TestCase.assertEquals(2399, myFixture.editor.caretModel.currentCaret.offset)
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Throws(Exception::class)
  @Test
  fun testSimpleCompletion() {
    checkBaseStringQualifiedCompletionWithTemplates {
      doTestWithCopyDirectory()
      myFixture.complete(
        CompletionType.BASIC)
    }
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testSimpleVueNoTs() {
    doTestWithCopyDirectory()
    myFixture.configureByFile("SimpleVue.vue")
    checkHighlightingByOptions(false)
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testSimpleVueEditing() {
    doTestWithCopyDirectory()
    myFixture.type('\b')
    checkAfterFile("vue")
    myFixture.type('s')
    checkAfterFile("2.vue")
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testSimpleVueEditingNoTs() {
    completeTsLangAndAssert()
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testSimpleVueEditingNoTsNoRefs() {
    completeTsLangAndAssert()
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testSimpleVueEditingCloseTag() {
    doTestWithCopyDirectory()
    myFixture.type('\b')
    checkAfterFile("vue")
    myFixture.type('/')
    checkAfterFile("2.vue")
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testSimpleVueTsx() {
    doTestWithCopyDirectory()
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testNoScriptSection() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    doTestWithCopyDirectory()
    myFixture.configureByFile("NoScriptSectionImport.vue")
    checkHighlightingByOptions(false)
  }


  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testFileCreation() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    myFixture.configureByFile("tsconfig.json")
    myFixture.configureByText("test.ts", "<error descr=\"TS2304: Cannot find name 'foo'.\">foo</error>")
    myFixture.checkHighlighting()
    myFixture.configureByText("test.vue", "<script lang='<caret>'></script>")
    myFixture.checkHighlighting()
    myFixture.type("ts")
    myFixture.moveToOffsetBySignature("><caret></")
    myFixture.type("<error descr=\"TS2304: Cannot find name 'foo'.\">foo</error")
    myFixture.checkHighlighting()
  }

  @TypeScriptVersion(TypeScriptVersions.TS36)
  @Test
  fun testScriptSetup() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    copyDirectory()
    myFixture.configureFileAndCheckHighlighting("ScriptSetup1.vue")
    myFixture.configureFileAndCheckHighlighting("ScriptSetup2.vue")
    myFixture.configureFileAndCheckHighlighting("ScriptSetup3.vue")
    myFixture.configureFileAndCheckHighlighting("ScriptSetup4.vue")
    myFixture.configureFileAndCheckHighlighting("ScriptSetup5.vue")
    myFixture.configureFileAndCheckHighlighting("ScriptSetupSkippedErrors.vue")
    myFixture.configureFileAndCheckHighlighting("ScriptSetupAwait.vue") // WEB-52317
    myFixture.configureFileAndCheckHighlighting("ScriptSetupAwaitTwoScripts.vue") // WEB-52317
  }

  @TypeScriptVersion(TypeScriptVersions.TS36)
  @Test
  fun testScriptSetupGeneric() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_0_ALPHA5)
    myFixture.configureByFile("tsconfig.json")
    // Required to load global declarations (defineProps) by the service
    myFixture.configureByText("import.ts", "import * from 'vue'")
    myFixture.configureByFile(getTestName(false) + ".vue")
    myFixture.checkHighlighting()
  }

  fun testTSErrorsInsideTemplate() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    copyDirectory()
    myFixture.configureFileAndCheckHighlighting("TSErrorsInsideTemplate.vue")
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testFileRename() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.configureByFile("tsconfig.json")
    myFixture.configureByText("bar.vue", "<script>foo</script>")
    myFixture.configureByText("foo.vue", "<script lang='ts'>import * from 'bar.vue'</script>")
    myFixture.configureByText("test.vue", "<script lang='ts'><error descr=\"TS2304: Cannot find name 'foo'.\">foo</error></script>")
    myFixture.checkHighlighting()

    //do the renaming
    val action = RenameElementAction()
    val e = TestActionEvent.createTestEvent(action) { name ->
      when (name) {
        DEFAULT_NAME.name -> "newTest.vue"
        PSI_ELEMENT.name -> myFixture.file
        PROJECT.name -> myFixture.project
        else -> null
      }
    }
    TestCase.assertTrue(ActionUtil.lastUpdateAndCheckDumb(action, e, true))
    ActionUtil.performActionDumbAwareWithCallbacks(action, e)
    TestCase.assertEquals("newTest.vue", myFixture.file.name)
    WriteAction.runAndWait<Throwable> {
      // We must set contents again, as previous call to `myFixture.checkHighlighting()` removed all markers.
      myFixture.getDocument(myFixture.file).setText(
        "<script lang='ts'><error descr=\"TS2304: Cannot find name 'bar'.\">bar</error></script>")
    }
    myFixture.checkHighlighting()
  }

  @TypeScriptVersion(TypeScriptVersions.TS36)
  @Test
  fun testScriptSetupImportResolveBothScripts() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    doTestWithCopyDirectory()
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testNoScriptSectionVue3() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    doTestWithCopyDirectory()
    myFixture.configureByFile("main.ts")
    checkHighlightByFile(myFixture, testDataPath + "/" + getTestName(false) + "/main_after.ts")
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testConfigScope() {
    doTestWithCopyDirectory()
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testSimpleVueTsToTsx() {
    doTestWithCopyDirectory()
    myFixture.type('x')
    checkAfterFile("vue")
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testSimpleVueTsxToTs() {
    doTestWithCopyDirectory()
    myFixture.type('\b')
    checkAfterFile("vue")
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testSimpleVueScriptChanges() {
    doTestWithCopyDirectory()
    repeat(10) {
      myFixture.type("x")
      checkHighlightByFile(myFixture, "$testDataPath/SimpleVueScriptChanges/test_tsx.vue")
      myFixture.type("\b\b\bjs")
      checkHighlightByFile(myFixture, "$testDataPath/SimpleVueScriptChanges/test_js.vue")
      myFixture.type("\b\bts")
      checkHighlightByFile(myFixture, "$testDataPath/SimpleVueScriptChanges/test_ts.vue")
    }
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Test
  fun testNoVueCompileOnSave() {
    val settings = TypeScriptCompilerSettings.getSettings(project)
    settings.isRecompileOnChanges = true
    settings.setUseServiceForProjectsWithoutConfig(true)
    settings.setUseService(true)
    myFixture.copyDirectoryToProject(getTestName(false), "")
    myFixture.configureFromTempProjectFile(getTestName(false) + "." + extension)
    myFixture.checkHighlighting()
    myFixture.configureFromTempProjectFile(getTestName(false) + "Clear." + extension)
    myFixture.type("1")
    myFixture.configureFromTempProjectFile("test.ts")
    myFixture.type("1")
    saveAllDocuments()
    myFixture.checkHighlighting()
    waitEmptyServiceQueue()
    refreshTempDir()
    val files = myFixture.tempDirFixture.findOrCreateDir(".")
      .children.asSequence().map { it.name }.sorted().toList()
    // There is race condition here and the files won't be compiled always.
    // However, if they are compiled we can ensure that there are no results for Vue files.
    if (files.contains("test.js.map")) {
      TestCase.assertEquals(listOf("NoVueCompileOnSave.vue", "NoVueCompileOnSaveClear.vue",
                                   "shims-vue.d.ts", "test.d.ts", "test.js", "test.js.map", "test.ts", "tsconfig.json"),
                            files)
    }
  }

  private fun saveAllDocuments() {
    // extracted due to test method isn't public: testNoVueCompileOnSave$lambda-5
    // another way to fix is to stop using JUnit 3
    WriteAction.runAndWait<Exception> {
      FileDocumentManager.getInstance().saveAllDocuments()
    }
  }

  private fun refreshTempDir() {
    // extracted due to test method isn't public: testNoVueCompileOnSave$lambda-5
    // another way to fix is to stop using JUnit 3
    WriteAction.runAndWait<Exception> {
      myFixture.tempDirFixture.findOrCreateDir(".").refresh(false, true)
    }
  }

  private fun completeTsLangAndAssert() {
    doTestWithCopyDirectory()
    myFixture.type(" lang=\"ts\"")
    FileDocumentManager.getInstance().saveDocument(myFixture.getDocument(myFixture.file))
    UIUtil.dispatchAllInvocationEvents()
    checkAfterFile("vue")
  }

  private fun CodeInsightTestFixture.configureFileAndCheckHighlighting(filePath: String) {
    val myFixture = this
    myFixture.configureFromTempProjectFile(filePath)
    myFixture.checkHighlighting()
  }

  companion object {
    private const val BASE_PATH = "/ts_ls_highlighting"
  }
}
