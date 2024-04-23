// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import com.intellij.codeInsight.intention.impl.ShowIntentionActionsHandler
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.modules.imports.JSImportAction
import com.intellij.lang.javascript.ui.NodeModuleNamesUtil
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil
import com.intellij.psi.util.PsiUtilBase
import com.intellij.testFramework.IndexingTestUtil.Companion.waitUntilIndexesAreReady
import com.intellij.testFramework.PsiTestUtil
import com.intellij.webSymbols.checkListByFile
import com.intellij.webSymbols.moveToOffsetBySignature
import org.angular2.Angular2MultiFileFixtureTestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureLink
import org.angular2.Angular2TestUtil
import org.angular2.inspections.AngularInvalidTemplateReferenceVariableInspection
import org.angular2.inspections.AngularUndefinedBindingInspection
import org.angular2.inspections.AngularUndefinedTagInspection
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import java.io.IOException
import java.util.*

/**
 * Also tests completion InsertHandlers.
 */
class Angular2NgModuleImportQuickFixesTest : Angular2MultiFileFixtureTestCase() {
  override fun getTestDataPath(): String {
    return Angular2TestUtil.getBaseTestDataPath() + "inspections/"
  }

  override fun getTestRoot(): String {
    return "ngModuleImport/"
  }

  fun testNgFor() {
    doMultiFileTest("angular-commons",
                    "test.html",
                    "*ng<caret>For",
                    "Import Angular entity...",
                    "CommonModule - \"@angular/common\"")
  }

  fun testNgForCompletion() {
    doCompletionTest("angular-commons",
                     "test.html",
                     "*ngFor=\"let item of items\"",
                     "*ngFo\nlet item of items",
                     "CommonModule - \"@angular/common\"")
  }

  fun testNgClass() {
    doMultiFileTest("angular-commons",
                    "test.html",
                    "[ng<caret>Class]",
                    "Import Angular entity...",
                    "CommonModule - \"@angular/common\"")
  }

  fun testNgClassCompletion() {
    doCompletionTest("angular-commons",
                     "test.html",
                     "[ngClass]=\"'my'\"",
                     "[ngCl\n'my'",
                     "CommonModule - \"@angular/common\"")
  }

  fun testLowercasePipe() {
    doMultiFileTest("angular-commons",
                    "test.html",
                    "lower<caret>case",
                    "Import Angular entity...",
                    "CommonModule - \"@angular/common\"")
  }

  fun testLowercasePipeCompletion() {
    doCompletionTest("angular-commons",
                     "test.html",
                     "lowercase",
                     "lo\n",
                     "CommonModule - \"@angular/common\"")
  }

  fun testImportDirective() {
    doMultiFileTest("test.html",
                    "Import Module2")
  }

  fun testImportDirectiveCompletion() {
    doTagCompletionTest("test.html", "Module2 - \"./module2\"")
  }

  fun testUndeclaredDirective() {
    doMultiFileTest("test.html",
                    "Declare MyDirective in MyModule")
  }

  fun testUndeclaredDirectiveCompletion() {
    doTagCompletionTest("test.html", "MyModule - \"./module\"")
  }

  fun testUndeclaredDirectiveDifferentModule() {
    doMultiFileTest("test.html",
                    "Declare MyDirective in Angular module",
                    "Module2 - \"./module2\"")
  }

  fun testUndeclaredDirectiveDifferentModuleCompletion() {
    doTagCompletionTest("test.html",
                        "Module2 - \"./module2\"")
  }

  fun testNotExportedDirectiveNoModuleImport() {
    doMultiFileTest("test.html",
                    "Export MyDirective")
  }

  fun testNotExportedDirectiveNoModuleImportCompletion() {
    doTagCompletionTest("test.html", null)
  }

  fun testNotExportedDirectiveSingleModuleImport() {
    doMultiFileTest("test.html",
                    "Export MyDirective")
  }

  fun testNotExportedDirectiveSingleModuleImportCompletion() {
    doTagCompletionTest("test.html", "Module2 - \"./module2\"")
  }

  fun testNotExportedDirectiveMultiModuleImport() {
    doMultiFileTest("test.html",
                    "Export MyDirective",
                    "Module3 - \"./module3\"")
  }

  fun testNotExportedDirectiveMultiModuleImportCompletion() {
    doTagCompletionTest("test.html",
                        "Module3 - \"./module3\"")
  }

  fun testInlineTemplate() {
    doMultiFileTest("component.ts",
                    "Declare MyDirective in MyModule")
  }

  fun testInlineTemplateCompletion() {
    doTagCompletionTest("component.ts", "MyModule - \"./module\"")
  }

  fun testFormsModule1() {
    doMultiFileTest("formsModule",
                    "test.html",
                    "[ngValue<caret>]",
                    "Import Angular entity...",
                    "FormsModule - \"@angular/forms\"",
                    ANGULAR_8)
  }

  fun testFormsModule2() {
    doMultiFileTest("formsModule",
                    "test.html",
                    "ng<caret>Model",
                    "Import Angular entity...",
                    "FormsModule - \"@angular/forms\"",
                    ANGULAR_8)
  }

  fun testFormsModule3() {
    doMultiFileTest("formsModule",
                    "test.html",
                    "[ng<caret>Model]",
                    "Import FormsModule",
                    null,
                    ANGULAR_8,
                    checkQuickFixList = true)
  }

  fun testFormsModule4() {
    doMultiFileTest("formsModule",
                    "test.html",
                    "[(ng<caret>Model)]",
                    "Import FormsModule",
                    null,
                    ANGULAR_8)
  }

  fun testFormsModuleCompletion1() {
    doCompletionTest("formsModule",
                     "test.html",
                     "[ngValue]=\"foo\"",
                     "[ngVal\nfoo",
                     "FormsModule - \"@angular/forms\"",
                     ANGULAR_8)
  }

  fun testFormsModuleCompletion2() {
    doCompletionTest("formsModule",
                     "test.html",
                     "ngModel ",
                     "ngMod\n ",
                     "FormsModule - \"@angular/forms\"",
                     ANGULAR_8)
  }

  fun testFormsModuleCompletion3() {
    doCompletionTest("formsModule",
                     "test.html",
                     "[ngModel]=\"foo\"",
                     "[ngMod\nfoo",
                     "FormsModule - \"@angular/forms\"",
                     ANGULAR_8)
  }

  fun testFormsModuleCompletion4() {
    doCompletionTest("formsModule",
                     "test.html",
                     "[(ngModel)]=\"foo\"",
                     "[(ngMod\nfoo",
                     "FormsModule - \"@angular/forms\"",
                     ANGULAR_8)
  }

  fun testReactiveFormsModule1() {
    doMultiFileTest("reactiveFormsModule",
                    "test.html",
                    "[ngValue<caret>]",
                    "Import Angular entity...",
                    "ReactiveFormsModule - \"@angular/forms\"",
                    ANGULAR_8)
  }

  fun testReactiveFormsModule2() {
    doMultiFileTest("reactiveFormsModule",
                    "test.html",
                    "ng<caret>Model",
                    "Import Angular entity...",
                    "ReactiveFormsModule - \"@angular/forms\"",
                    ANGULAR_8)
  }

  fun testReactiveFormsModuleCompletion1() {
    doCompletionTest("reactiveFormsModule",
                     "test.html",
                     "[ngValue]=\"foo\"",
                     "[ngVal\nfoo",
                     "ReactiveFormsModule - \"@angular/forms\"",
                     ANGULAR_8)
  }

  fun testReactiveFormsModuleCompletion2() {
    doCompletionTest("reactiveFormsModule",
                     "test.html",
                     "ngModel ",
                     "ngMod\n ",
                     "ReactiveFormsModule - \"@angular/forms\"",
                     ANGULAR_8)
  }

  fun testLocalLib() {
    doMultiFileTest("src/app/app.component.html",
                    "Import MyLibModule") {
      excludeDistDir()
    }
  }

  fun testLocalLibCompletion() {
    doCompletionTest("localLib", "src/app/app.component.html",
                     "lib-my-lib", "lib-my-l\n",
                     "MyLibModule - \"my-lib\"") {
      excludeDistDir()
    }
  }

  fun testImportStandaloneComponentToStandaloneComponent() {
    doMultiFileTest("standaloneComponent",
                    "test.ts",
                    "app-<caret>standalone",
                    "Import StandaloneComponent",
                    "StandaloneComponent - \"./standalone.component\"")
  }

  fun testImportStandalonePipeToStandaloneComponent() {
    doMultiFileTest("standalonePipe",
                    "test.ts",
                    "stand<caret>alone",
                    "Import StandalonePipe",
                    "StandalonePipe - \"./standalone.pipe\"")
  }

  fun testImportStandaloneComponentToModule() {
    doMultiFileTest("standaloneComponentToModule",
                    "test.ts",
                    "app-<caret>standalone",
                    "Import StandaloneComponent",
                    "StandaloneComponent - \"./standalone.component\"")
  }

  fun testImportStandaloneComponentImportModule() {
    doMultiFileTest("standaloneComponentImportModule",
                    "test.ts",
                    "app-<caret>classic",
                    "Import ClassicModule",
                    "ClassicModule - \"./classic\"")
  }

  fun testLocalLibraryWithAlias() {
    doMultiFileTest("projects/demo/src/app/app.component.html",
                    "Import Lib1Module"
    )
  }

  fun testSameFileStandaloneDirectiveToComponent() {
    doMultiFileTest("test.ts",
                    "Import TestDir"
    )
  }

  fun testImportDirectiveFromInterpolationBinding() {
    doMultiFileTest("hero-search.component.html",
                    "Import RouterLink",
                    modules = ANGULAR_16
    )
  }

  fun testImportStandalonePseudoModuleToStandaloneComponent() {
    doMultiFileTest("standalonePseudoModule",
                    "check.html",
                    "<fo<caret>o>",
                    "Import FooComponent",
                    "FOO_COMPONENT_EXPORT_DECLARE_CONST_READ_ONLY - \"./foo\"",
                    expectedImports = setOf("FooComponent - \"./foo\"",
                                            "FOO_COMPONENT_EXPORT_DECLARE_CONST_READ_ONLY - \"./foo\"",
                                            "FOO_COMPONENT_EXPORT_DECLARE_CONST - \"./foo\"",
                                            "FOO_COMPONENT_EXPORT_CONST - \"./foo\"",
                                            "FOO_COMPONENT_EXPORT_CONST_AS_CONST - \"./foo\""))
  }

  fun testLibComponent() {
    doMultiFileTest("app.component.ts",
                    "Import SharedComponent") {
      excludeDistDir()
    }
  }

  private fun doMultiFileTest(mainFile: String,
                              intention: String,
                              importName: String? = null,
                              modules: Array<out Angular2TestModule> = ANGULAR_4,
                              configure: () -> Unit = {}) {
    doMultiFileTest(getTestName(true), mainFile, null,
                    intention, importName, modules, configure = configure)
  }

  private fun doMultiFileTest(testName: String,
                              mainFile: String,
                              signature: String?,
                              intention: String,
                              importName: String?,
                              modules: Array<out Angular2TestModule> = ANGULAR_4,
                              checkQuickFixList: Boolean = false,
                              expectedImports: Set<String> = emptySet(),
                              configure: () -> Unit = {}) {
    initInspections()
    doTest(
      { _, _ ->
        configure()
        configureTestAndRun(mainFile, signature, importName, expectedImports, modules, Runnable {
          if (checkQuickFixList) {
            myFixture.availableIntentions // load intentions
            val intentions = ShowIntentionActionsHandler.calcCachedIntentions(myFixture.getProject(), getHostEditor(), getHostFileAtCaret())
            myFixture.checkListByFile((intentions.allActions.apply { retainAll(intentions.errorFixes + intentions.inspectionFixes) })
                                        .map { it.text },
                                      "$testName.quickFixes.txt", false)
          }
          myFixture.launchAction(myFixture.findSingleIntention(intention))
        })
      }, testName)
  }

  private fun doTagCompletionTest(mainFile: String,
                                  importToSelect: String?) {
    doCompletionTest(getTestName(true).removeSuffix("Completion"),
                     mainFile, "foo", "foo\n", importToSelect)
  }

  private fun doCompletionTest(testName: String,
                               mainFile: String,
                               toRemove: String,
                               toType: String,
                               importToSelect: String?,
                               modules: Array<out Angular2TestModule> = ANGULAR_4,
                               configure: () -> Unit = {}) {
    doTest(
      { _, _ ->
        configure()
        configureTestAndRun(mainFile, "<caret>$toRemove", importToSelect, emptySet(), modules, Runnable {
          myFixture.getEditor().putUserData(Angular2FixesFactory.DECLARATION_TO_CHOOSE, "MyDirective")
          myFixture.getEditor().getSelectionModel().setSelection(myFixture.getCaretOffset(),
                                                                 myFixture.getCaretOffset() + toRemove.length)
          myFixture.type("\b")
          myFixture.completeBasic()
          myFixture.type(toType)
        })
      }, testName)
  }

  @Throws(IOException::class)
  private fun configureTestAndRun(mainFile: String, signature: String?, importName: String?,
                                  expectedImports: Set<String>, modules: Array<out Angular2TestModule>,
                                  runnable: Runnable) {
    val hasPkgJson = myFixture.getTempDirFixture().getFile(NodeModuleNamesUtil.PACKAGE_JSON) != null
    configureLink(myFixture, *modules)
    myFixture.configureFromTempProjectFile(mainFile)
    if (signature != null) {
      myFixture.moveToOffsetBySignature(signature)
    }
    if (importName != null) {
      myFixture.getEditor().putUserData(JSImportAction.NAME_TO_IMPORT, importName)
    }
    if (expectedImports.isNotEmpty()) {
      myFixture.getEditor().putUserData(JSImportAction.EXPECTED_NAMES_TO_IMPORT, expectedImports)
    }
    runnable.run()
    if (!hasPkgJson) {
      WriteAction.runAndWait<IOException> { myFixture.getTempDirFixture().getFile(NodeModuleNamesUtil.PACKAGE_JSON)!!.delete(null) }
    }
  }

  private fun excludeDistDir() {
    val dist = myFixture.findFileInTempDir("dist")!!
    PsiTestUtil.addExcludedRoot(module, dist)
    waitUntilIndexesAreReady(module.project)
    TypeScriptTestUtil.waitForLibraryUpdate(myFixture)
    Disposer.register(testRootDisposable) {
      PsiTestUtil.removeExcludedRoot(module, dist)
    }
  }

  private fun initInspections() {
    myFixture.enableInspections(
      AngularUndefinedBindingInspection::class.java,
      AngularUndefinedTagInspection::class.java,
      AngularInvalidTemplateReferenceVariableInspection::class.java,
      TypeScriptUnresolvedReferenceInspection::class.java
    )
  }

  private fun getHostEditor(): Editor {
    return InjectedLanguageEditorUtil.getTopLevelEditor(myFixture.getEditor())
  }

  private fun getHostFileAtCaret(): PsiFile {
    return PsiUtilBase.getPsiFileInEditor(getHostEditor(), project)!!
  }


  companion object {
    private val ANGULAR_4 = arrayOf(Angular2TestModule.ANGULAR_CORE_4_0_0, Angular2TestModule.ANGULAR_COMMON_4_0_0,
                                    Angular2TestModule.ANGULAR_PLATFORM_BROWSER_4_0_0)
    private val ANGULAR_8 = arrayOf(Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14,
                                    Angular2TestModule.ANGULAR_FORMS_8_2_14)
    private val ANGULAR_16 = arrayOf(Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8,
                                     Angular2TestModule.ANGULAR_FORMS_16_2_8, Angular2TestModule.ANGULAR_ROUTER_16_2_8)

  }
}
