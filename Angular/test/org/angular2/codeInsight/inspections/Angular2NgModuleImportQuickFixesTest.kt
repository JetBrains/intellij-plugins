// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import com.intellij.codeInsight.intention.impl.ShowIntentionActionsHandler
import com.intellij.javascript.testFramework.web.WebFrameworkTestConfigurator
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.modules.imports.JSImportAction
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil
import com.intellij.psi.util.PsiUtilBase
import com.intellij.testFramework.IndexingTestUtil.Companion.waitUntilIndexesAreReady
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.webSymbols.testFramework.checkListByFile
import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.inspections.AngularInvalidTemplateReferenceVariableInspection
import org.angular2.inspections.AngularUndefinedBindingInspection
import org.angular2.inspections.AngularUndefinedTagInspection
import org.angular2.inspections.AngularUnresolvedPipeInspection
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import java.io.IOException

/**
 * Also tests completion InsertHandlers.
 */
class Angular2NgModuleImportQuickFixesTest : Angular2TestCase("inspections/ngModuleImport", true) {

  fun testNgFor() {
    doMultiFileTest("angular-commons",
                    "test.html",
                    "*ng<caret>For",
                    "Import CommonModule",
                    "CommonModule - \"@angular/common\"",
                    modules = ANGULAR_13)
  }

  fun testNgForCompletion() {
    doCompletionTest("angular-commons",
                     "test.html",
                     "*ngFor=\"let item of items\"",
                     "*ngFo\nlet item of items",
                     "CommonModule - \"@angular/common\"",
                     modules = ANGULAR_13)
  }

  fun testNgClass() {
    doMultiFileTest("angular-commons",
                    "test.html",
                    "[ng<caret>Class]",
                    "Import CommonModule",
                    "CommonModule - \"@angular/common\"",
                    modules = ANGULAR_13)
  }

  fun testNgClassCompletion() {
    doCompletionTest("angular-commons",
                     "test.html",
                     "[ngClass]=\"'my'\"",
                     "[ngCl\n'my'",
                     "CommonModule - \"@angular/common\"",
                     modules = ANGULAR_13)
  }

  fun testLowercasePipe() {
    doMultiFileTest("angular-commons",
                    "test.html",
                    "lower<caret>case",
                    "Import CommonModule",
                    "CommonModule - \"@angular/common\"",
                    modules = ANGULAR_13)
  }

  fun testLowercasePipeCompletion() {
    doCompletionTest("angular-commons",
                     "test.html",
                     "lowercase",
                     "lo\n",
                     "CommonModule - \"@angular/common\"",
                     modules = ANGULAR_13)
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
                    "FormsModule - \"@angular/forms\"")
  }

  fun testFormsModule2() {
    doMultiFileTest("formsModule",
                    "test.html",
                    "ng<caret>Model",
                    "Import Angular entity...",
                    "FormsModule - \"@angular/forms\"")
  }

  fun testFormsModule3() {
    doMultiFileTest("formsModule",
                    "test.html",
                    "[ng<caret>Model]",
                    "Import FormsModule",
                    null,
                    checkQuickFixList = true)
  }

  fun testFormsModule4() {
    doMultiFileTest("formsModule",
                    "test.html",
                    "[(ng<caret>Model)]",
                    "Import FormsModule",
                    null)
  }

  fun testFormsModuleCompletion1() {
    doCompletionTest("formsModule",
                     "test.html",
                     "[ngValue]=\"foo\"",
                     "[ngVal\nfoo",
                     "FormsModule - \"@angular/forms\"")
  }

  fun testFormsModuleCompletion2() {
    doCompletionTest("formsModule",
                     "test.html",
                     "ngModel ",
                     "ngMod\n ",
                     "FormsModule - \"@angular/forms\"")
  }

  fun testFormsModuleCompletion3() {
    doCompletionTest("formsModule",
                     "test.html",
                     "[ngModel]=\"foo\"",
                     "[ngMod\nfoo",
                     "FormsModule - \"@angular/forms\"")
  }

  fun testFormsModuleCompletion4() {
    doCompletionTest("formsModule",
                     "test.html",
                     "[(ngModel)]=\"foo\"",
                     "[(ngMod\nfoo",
                     "FormsModule - \"@angular/forms\"")
  }

  fun testReactiveFormsModule1() {
    doMultiFileTest("reactiveFormsModule",
                    "test.html",
                    "[ngValue<caret>]",
                    "Import Angular entity...",
                    "ReactiveFormsModule - \"@angular/forms\"")
  }

  fun testReactiveFormsModule2() {
    doMultiFileTest("reactiveFormsModule",
                    "test.html",
                    "ng<caret>Model",
                    "Import Angular entity...",
                    "ReactiveFormsModule - \"@angular/forms\"")
  }

  fun testReactiveFormsModuleCompletion1() {
    doCompletionTest("reactiveFormsModule",
                     "test.html",
                     "[ngValue]=\"foo\"",
                     "[ngVal\nfoo",
                     "ReactiveFormsModule - \"@angular/forms\"")
  }

  fun testReactiveFormsModuleCompletion2() {
    doCompletionTest("reactiveFormsModule",
                     "test.html",
                     "ngModel ",
                     "ngMod\n ",
                     "ReactiveFormsModule - \"@angular/forms\"")
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
                    "Import RouterLink")
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

  fun testIonicStandaloneComponent() {
    doMultiFileTest("ionicStandaloneComponent",
                    "folder.page.html",
                    "<ion-<caret>content [f",
                    "Import Angular entity...",
                    "IonContent - \"@ionic/angular/standalone\"",
                    modules = arrayOf(
                      Angular2TestModule.ANGULAR_CORE_17_3_0,
                      Angular2TestModule.IONIC_ANGULAR_7_7_3
                    ),
                    expectedImports = setOf("IonContent - \"@ionic/angular/standalone\"",
                                            "IonicModule - \"@ionic/angular\"")
    )
  }

  private fun doMultiFileTest(
    mainFile: String,
    intention: String,
    importName: String? = null,
    configure: () -> Unit = {},
  ) {
    doMultiFileTest(getTestName(true), mainFile, null,
                    intention, importName, configure = configure)
  }

  private fun doMultiFileTest(
    testName: String,
    mainFile: String,
    signature: String?,
    intention: String,
    importName: String?,
    modules: Array<out Angular2TestModule> = ANGULAR_16,
    checkQuickFixList: Boolean = false,
    expectedImports: Set<String> = emptySet(),
    configure: (() -> Unit)? = null,
  ) {
    initInspections()
    doConfiguredTest(*modules, dir = true, dirName = testName, configureFileName = mainFile, checkResult = true,
                     configurators = buildConfiguratorsList(configure)) {
      setUpEditor(signature, importName, expectedImports)
      if (checkQuickFixList) {
        myFixture.availableIntentions // load intentions
        val intentions = ShowIntentionActionsHandler.calcCachedIntentions(myFixture.getProject(), getHostEditor(), getHostFileAtCaret())
        myFixture.checkListByFile((intentions.allActions.apply { retainAll(intentions.errorFixes + intentions.inspectionFixes) })
                                    .map { it.text },
                                  "$testName.quickFixes.txt", false)
      }
      myFixture.launchAction(myFixture.findSingleIntention(intention))
    }
  }

  private fun doTagCompletionTest(
    mainFile: String,
    importToSelect: String?,
  ) {
    doCompletionTest(getTestName(true).removeSuffix("Completion"),
                     mainFile, "foo", "foo\n", importToSelect)
  }

  private fun doCompletionTest(
    testName: String,
    mainFile: String,
    toRemove: String,
    toType: String,
    importToSelect: String?,
    modules: Array<out Angular2TestModule> = ANGULAR_16,
    configure: (() -> Unit)? = null,
  ) {
    doConfiguredTest(*modules, dir = true, dirName = testName, configureFileName = mainFile, checkResult = true,
                     configurators = buildConfiguratorsList(configure)) {
      setUpEditor("<caret>$toRemove", importToSelect, emptySet())
      myFixture.getEditor().putUserData(Angular2FixesFactory.DECLARATION_TO_CHOOSE, "MyDirective")
      myFixture.getEditor().getSelectionModel().setSelection(myFixture.getCaretOffset(),
                                                             myFixture.getCaretOffset() + toRemove.length)
      myFixture.type("\b")
      myFixture.completeBasic()
      myFixture.type(toType)
    }
  }

  private fun buildConfiguratorsList(configure: (() -> Unit)? = null): List<WebFrameworkTestConfigurator> =
    if (configure != null)
      listOf(object : WebFrameworkTestConfigurator {
        override fun configure(fixture: CodeInsightTestFixture, disposable: Disposable?) {
          configure()
        }
      })
    else emptyList()

  @Throws(IOException::class)
  private fun setUpEditor(
    signature: String?, importName: String?, expectedImports: Set<String>,
  ) {
    if (signature != null) {
      myFixture.moveToOffsetBySignature(signature)
    }
    if (importName != null) {
      myFixture.getEditor().putUserData(JSImportAction.NAME_TO_IMPORT, importName)
    }
    if (expectedImports.isNotEmpty()) {
      myFixture.getEditor().putUserData(JSImportAction.EXPECTED_NAMES_TO_IMPORT, expectedImports)
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
      TypeScriptUnresolvedReferenceInspection::class.java,
      AngularUnresolvedPipeInspection::class.java,
    )
  }

  private fun getHostEditor(): Editor {
    return InjectedLanguageEditorUtil.getTopLevelEditor(myFixture.getEditor())
  }

  private fun getHostFileAtCaret(): PsiFile {
    return PsiUtilBase.getPsiFileInEditor(getHostEditor(), project)!!
  }


  companion object {
    private val ANGULAR_13 = arrayOf(Angular2TestModule.ANGULAR_CORE_13_3_5, Angular2TestModule.ANGULAR_COMMON_13_3_5)
    private val ANGULAR_16 = arrayOf(Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8,
                                     Angular2TestModule.ANGULAR_FORMS_16_2_8, Angular2TestModule.ANGULAR_ROUTER_16_2_8)

  }
}
