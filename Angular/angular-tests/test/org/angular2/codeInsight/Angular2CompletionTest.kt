// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.openapi.application.ApplicationManager
import com.intellij.platform.lsp.tests.waitUntilFileOpenedByLspServer
import com.intellij.polySymbols.testFramework.LookupElementInfo
import com.intellij.polySymbols.testFramework.checkLookupItems
import com.intellij.polySymbols.testFramework.enableIdempotenceChecksOnEveryCache
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.ANGULAR_CORE_13_3_5
import org.angular2.Angular2TestModule.ANGULAR_CORE_15_1_5
import org.angular2.Angular2TestModule.ANGULAR_CORE_19_2_0
import org.angular2.Angular2TestModule.ANGULAR_CORE_21_2_0
import org.angular2.Angular2TestModule.IONIC_ANGULAR_8_4_3
import org.angular2.Angular2TsConfigFile
import org.angular2.SkipTsGoFork
import org.angular2.TestTsGoFork
import org.angular2.TestTsNode
import org.angular2.lang.Angular2Bundle
import org.junit.Test

@TestTsNode
@TestTsGoFork
class Angular2CompletionTest : Angular2TestCase("completion") {

  override fun setUp() {
    super.setUp()
    // Let's ensure we don't get PolySymbols registry stack overflows randomly
    this.enableIdempotenceChecksOnEveryCache()
  }

  @Test
  fun testExportAs() =
    doLookupTest(checkDocumentation = true)

  @Test
  fun testRecursiveHostDirective() =
    doLookupTest(locations = listOf("ref-a=\"<caret>\"", " [we<caret>]>"))

  @Test
  fun testHostDirectivesProperties() =
    doLookupTest {
      it.priority == 100.0
    }

  @Test
  fun testHostDirectiveInputMapping() =
    doLookupTest(renderTypeText = true, renderPriority = false)

  @Test
  fun testHostDirectiveInputMappingOutsideLiteral() =
    doLookupTest(renderTypeText = true, renderPriority = false, renderPresentedText = true) {
      it.priority >= 90
    }

  @Test
  fun testHostDirectiveOutputMapping() =
    doLookupTest(ANGULAR_CORE_13_3_5, renderTypeText = true, renderPriority = false)

  @Test
  fun testHostDirectiveInputMappingWithReplace() =
    doTypingTest("vir\t")

  @Test
  fun testDirectiveInputMappingLiteralWithReplace() =
    doTypingTest("ie\t")

  @Test
  fun testDirectiveInputMappingLiteral() =
    doLookupTest()

  @Test
  fun testDirectiveInputMappingOutsideLiteral() =
    doLookupTest(renderPresentedText = true) {
      it.priority >= 100
    }

  @Test
  fun testDirectiveInputMappingObject() =
    doLookupTest()

  @Test
  fun testDirectiveInputMappingOutsideObject() =
    doLookupTest(renderPresentedText = true) {
      it.priority >= 100
    }

  @Test
  fun testDirectiveOutputMapping() =
    doLookupTest(ANGULAR_CORE_13_3_5)

  @Test
  fun testSignal() =
    doBasicCompletionTest(Angular2TestModule.ANGULAR_CORE_16_2_8)

  // TODO WEB-67260 - fix completion of custom signals
  fun _testCustomSignal() =
    doBasicCompletionTest(Angular2TestModule.ANGULAR_CORE_16_2_8)

  // TODO WEB-67260 - fix completion of signal calls
  fun _testSignalInGenericStructuralDirective() =
    doBasicCompletionTest(Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8,
                          dir = true, extension = "html")

  // TODO WEB-67260 - fix completion of signal calls
  fun _testSignalInGenericDirective() =
    doBasicCompletionTest(Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8,
                          dir = true, extension = "html")

  @Test
  fun testNotSignal() =
    doBasicCompletionTest(Angular2TestModule.ANGULAR_CORE_16_2_8)

  @Test
  fun testTemplatesCompletion() =
    doLookupTest(Angular2TestModule.ANGULAR_COMMON_4_0_0, extension = "html")

  @Test
  fun testTemplatesCompletion16() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8, extension = "html",
                 configurators = listOf(Angular2TsConfigFile(strict = false)))

  @Test
  fun testTemplatesCompletion16Strict() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8,
                 extension = "html", configurators = listOf(Angular2TsConfigFile()))

  @Test
  fun testPrimaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testPrimaryBlocksTopLevelText() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", lookupItemFilter = ::notAnElement)

  @Test
  fun testPrimaryBlocksNestedText() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", lookupItemFilter = ::notAnElement)

  @Test
  fun testIfSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testIfSecondaryBlocksNested() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testIfSecondaryBlocksTopLevelText() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", lookupItemFilter = ::notAnElement)

  @Test
  fun testIfSecondaryBlocksNestedText() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", lookupItemFilter = ::notAnElement)

  @Test
  fun testAfterElseBlock() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testDeferSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testDeferSecondaryBlocksUnique() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testForSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testForSecondaryBlocksNested() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testForSecondaryBlocksUnique() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testSwitchSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testSwitchNoSiblingSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testSwitchSecondaryBlocksUnique() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testIfBlock() =
    doTypingTest("if\n", Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  fun testErrorBlock() =
    doTypingTest("err\n", Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  fun testTagsWithinBlock() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testTagsWithinBlock2() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testNoGlobalImportInTsFiles() =
    doTypingTest("do1\n", dir = true)

  @Test
  fun testIfBlockParameters() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", checkDocumentation = true)

  @Test
  fun testIfBlockParameterTyping() =
    doTypingTest("\nfoo", Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  fun testForBlockOfKeyword() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testForBlockParams() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", checkDocumentation = true)

  @Test
  fun testForBlockLetEqKeyword() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testForBlockImplicitVariables() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testForBlockImplicitVariableInExpr() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", checkDocumentation = true,
                 lookupItemFilter = { it.lookupString == "\$count" })

  @Test
  fun testForBlockVariablesInExpr() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html",
                 lookupItemFilter = { it.priority >= 100 })

  @Test
  fun testDeferBlockTimeExpressionStart() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testDeferBlockTimeExpressionTimeUnit() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testDeferBlockParameters() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testDeferBlockParametersNg19() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_19_2_0, extension = "html")

  @Test
  fun testDeferBlockPrefetchParameters() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testDeferBlockHydrateParameters() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_19_2_0, extension = "html")

  @Test
  fun testDeferBlockOnTriggers() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testDeferBlockOnTimerNoArgCompletion() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testDeferBlockOnViewport() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testKeyofAttribute() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, configurators = listOf(Angular2TsConfigFile()))

  @Test
  @SkipTsGoFork
  fun testTsconfigPriority() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", dir = true, configureFileName = "src/component.html") {
      it.priority >= 100
    }

  @Test
  fun testStructuralDirectiveWithInjectedFields() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  fun testLetBlockTemplate() {
    TemplateManagerImpl.setTemplateTesting(testRootDisposable)
    doTypingTest("le\nfoo\t12\n", Angular2TestModule.ANGULAR_CORE_18_2_1, extension = "html")
  }

  @Test
  fun testLetBlockExpression() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_18_2_1, extension = "html") {
      it.priority >= 100
    }

  @Test
  fun testHostBindingJSProperty1() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts") {
      it.lookupString.startsWith("ti")
      || it.lookupString.startsWith("[s")
      || it.lookupString.startsWith("(cl")
    }

  @Test
  fun testHostBindingJSProperty2() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testHostBindingCssClass1() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testHostBindingCssClass2() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  @SkipTsGoFork //new gold file
  fun testHostBindingDecorator1() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testHostBindingDecorator2() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testHostListenerDecorator() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  @SkipTsGoFork
  fun testObjectInitializerProperties() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts",
                 locations = listOf("[product]=\"{<caret>}\"", "[product]=\"{title,<caret>}\""))

  @Test
  fun testViewChildrenDecorator() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts",
                 locations = listOf("@ViewChild('<caret>area')", "@ViewChildren('<caret>area')"))

  @Test
  fun testViewChildrenDecoratorHtml() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts", dir = true,
                 locations = listOf("@ViewChild('<caret>area')", "@ViewChildren('<caret>area')"))

  @Test
  fun testViewChildrenSignal() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts",
                 locations = (1..6).map { "\"<caret>area$it\"" })

  @Test
  @SkipTsGoFork //new gold file
  fun testSignalStore() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_20_1_4, Angular2TestModule.NGRX_SIGNALS_20_1_0, extension = "ts",
                 configurators = listOf(Angular2TsConfigFile()))

  @Test
  @SkipTsGoFork
  fun testTemplateBindingsNgIf() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, Angular2TestModule.ANGULAR_COMMON_17_3_0, extension = "ts",
                 lookupItemFilter = { it.priority > 0 && it.lookupString != "Component" },
                 locations = listOf(
                   "<div *ngIf=\"<caret>true as foo; else foo as sss; let car = ngIf\"></div>",
                   "<div *ngIf=\"true as <caret>foo; else foo as sss; let car = ngIf\"></div>",
                   "<div *ngIf=\"true as foo; <caret>else foo as sss; let car = ngIf\"></div>",
                   "<div *ngIf=\"true as foo; else <caret>foo as sss; let car = ngIf\"></div>",
                   "<div *ngIf=\"true as foo; else foo as <caret>sss; let car = ngIf\"></div>",
                   "<div *ngIf=\"true as foo; else foo as sss; <caret>let car = ngIf\"></div>",
                   "<div *ngIf=\"true as foo; else foo as sss; let <caret>car = ngIf\"></div>",
                   "<div *ngIf=\"true as foo; else foo as sss; let car = <caret>ngIf\"></div>",
                 ))

  @Test
  @SkipTsGoFork
  fun testTemplateBindingsNgFor() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, Angular2TestModule.ANGULAR_COMMON_17_3_0, extension = "ts",
                 lookupItemFilter = { it.priority > 0 && it.lookupString != "Component" },
                 locations = listOf(
                   "<div *ngFor=\"<caret>let foo of [1,2,3]; trackBy: trackByFn; let index = index\"></div>",
                   "<div *ngFor=\"let <caret>foo of [1,2,3]; trackBy: trackByFn; let index = index\"></div>",
                   "<div *ngFor=\"let foo <caret>of [1,2,3]; trackBy: trackByFn; let index = index\"></div>",
                   "<div *ngFor=\"let foo of <caret>[1,2,3]; trackBy: trackByFn; let index = index\"></div>",
                   "<div *ngFor=\"let foo of [1,2,3]; <caret>trackBy: trackByFn; let index = index\"></div>",
                   "<div *ngFor=\"let foo of [1,2,3]; trackBy: <caret>trackByFn; let index = index\"></div>",
                   "<div *ngFor=\"let foo of [1,2,3]; trackBy: trackByFn; <caret>let index = index\"></div>",
                   "<div *ngFor=\"let foo of [1,2,3]; trackBy: trackByFn; let <caret>index = index\"></div>",
                   "<div *ngFor=\"let foo of [1,2,3]; trackBy: trackByFn; let index = <caret>index\"></div>",
                 ))

  @Test
  @SkipTsGoFork
  fun testTemplateBindingsNgForContextDocumentation() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1, extension = "ts",
                 lookupItemFilter = { it.lookupString == "index" || it.lookupString == "last" || it.lookupString == "ngForOf" },
                 checkDocumentation = true)

  @Test
  fun testNewAngularAnimationBindings() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_20_2_2, extension = "ts", checkDocumentation = true)

  @Test
  fun testCssCustomProperty() =
    doLookupTest(ANGULAR_CORE_19_2_0, extension = "ts", checkDocumentation = true, dir = true)

  @Test
  @SkipTsGoFork
  fun testTailwindInNgClass() =
    doConfiguredTest(ANGULAR_CORE_19_2_0, Angular2TestModule.TAILWINDCSS_4_1_7, extension = "ts", dir = true,
                     configureFileName = "src/tailwindInNgClass.ts") {
      waitUntilFileOpenedByLspServer(getProject(), getFile().getVirtualFile())
      checkLookupItems(renderTypeText = true)
    }

  @Test
  fun testNoKeyupEventCodeModifierNg13() =
    doLookupTest(ANGULAR_CORE_13_3_5, extension = "html") {
      it.lookupString.endsWith(".")
    }

  @Test
  fun testKeyupEventCodeModifierNg15() =
    doLookupTest(ANGULAR_CORE_15_1_5, extension = "html") {
      it.lookupString.endsWith(".")
    }

  @Test
  @SkipTsGoFork
  fun testNarrowingInCaseBlock() =
    doLookupTest(ANGULAR_CORE_19_2_0, extension = "html", dir = true, configurators = listOf(Angular2TsConfigFile())) {
      it.priority > 0.0
    }

  @Test
  fun testAutoImportWithDFilesNg19() =
    doEditorTypingTest(ANGULAR_CORE_19_2_0, configureFileName = "autoImportWithDFiles.ts", checkResult = true) {
      completeBasic()
      type("SetFn\n")
    }

  @Test
  fun testAutoImportWithDFilesNg20_1() =
    doEditorTypingTest(Angular2TestModule.ANGULAR_CORE_20_1_4, configureFileName = "autoImportWithDFiles.ts", checkResult = true) {
      completeBasic()
      type("SetFn\n")
    }

  @Test
  fun testCompleteAfterQuickFixToExternalTemplate() {
    myFixture.setCaresAboutInjection(false)
    doEditorTypingTest(ANGULAR_CORE_19_2_0, checkResult = true, configurators = listOf(Angular2TsConfigFile())) {
      ApplicationManager.getApplication().invokeAndWait {
        ApplicationManager.getApplication().runWriteIntentReadAction<Unit, Throwable> {
          myFixture.launchAction(Angular2Bundle.message("angular.intention.extract.component.template.name"))
          myFixture.openFileInEditor(myFixture.tempDirFixture.getFile("completeAfterQuickFixToExternalTemplate.html")!!)
        }
      }
      moveToOffsetBySignature("[product]=\"{ <caret> }\"")
      completeBasic()
      assertLookupContains("description")
      type("des\n")
    }
  }

  @Test
  fun testArrowFunctions() {
    doLookupTest(ANGULAR_CORE_21_2_0, locations = listOf(
      "() => <caret>componentProp + 1",
      "(a,b) => <caret>val + 1"
    )) {
      it.priority > 10.0
    }
  }

  @Test
  @SkipTsGoFork //new gold file
  fun testComponentLifecycleHooks() =
    doLookupTest(ANGULAR_CORE_21_2_0, renderTailText = true)

  @Test
  @SkipTsGoFork //new gold file
  fun testIonicLifecycleHooks() =
    doLookupTest(ANGULAR_CORE_21_2_0, IONIC_ANGULAR_8_4_3, renderTailText = true)

  private fun notAnElement(it: LookupElementInfo): Boolean = !it.lookupString.startsWith("<")

  private fun doBasicCompletionTest(vararg modules: WebFrameworkTestModule, dir: Boolean = false, extension: String = "ts") =
    doTypingTest(null, *modules, dir = dir, extension = extension)

  private fun doTypingTest(toType: String? = null, vararg modules: WebFrameworkTestModule, dir: Boolean = false, extension: String = "ts") {
    doConfiguredTest(*modules, checkResult = true, dir = dir, extension = extension) {
      completeBasic()
      if (toType != null) type(toType)
    }
  }

}