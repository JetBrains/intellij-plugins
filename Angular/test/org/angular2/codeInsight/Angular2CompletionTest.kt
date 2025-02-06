// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.webSymbols.testFramework.LookupElementInfo
import com.intellij.webSymbols.testFramework.enableIdempotenceChecksOnEveryCache
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.ANGULAR_CORE_13_3_5
import org.angular2.Angular2TsConfigFile

class Angular2CompletionTest : Angular2TestCase("completion", true) {

  override fun setUp() {
    super.setUp()
    // Let's ensure we don't get WebSymbols registry stack overflows randomly
    this.enableIdempotenceChecksOnEveryCache()
  }

  fun testExportAs() =
    doLookupTest(checkDocumentation = true)

  fun testRecursiveHostDirective() =
    doLookupTest(locations = listOf("ref-a=\"<caret>\"", " [we<caret>]>"))

  fun testHostDirectivesProperties() =
    doLookupTest {
      it.priority == 100.0
    }

  fun testHostDirectiveInputMapping() =
    doLookupTest(renderTypeText = true, renderPriority = false)

  fun testHostDirectiveInputMappingOutsideLiteral() =
    doLookupTest(renderTypeText = true, renderPriority = false, renderPresentedText = true) {
      it.priority >= 100
    }

  fun testHostDirectiveOutputMapping() =
    doLookupTest(ANGULAR_CORE_13_3_5, renderTypeText = true, renderPriority = false)

  fun testHostDirectiveInputMappingWithReplace() =
    doTypingTest("vir\t")

  fun testDirectiveInputMappingLiteralWithReplace() =
    doTypingTest("ie\t")

  fun testDirectiveInputMappingLiteral() =
    doLookupTest()

  fun testDirectiveInputMappingOutsideLiteral() =
    doLookupTest(renderPresentedText = true) {
      it.priority >= 100
    }

  fun testDirectiveInputMappingObject() =
    doLookupTest()

  fun testDirectiveInputMappingOutsideObject() =
    doLookupTest(renderPresentedText = true) {
      it.priority >= 100
    }

  fun testDirectiveOutputMapping() =
    doLookupTest(ANGULAR_CORE_13_3_5)

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

  fun testNotSignal() =
    doBasicCompletionTest(Angular2TestModule.ANGULAR_CORE_16_2_8)

  fun testTemplatesCompletion() =
    doLookupTest(Angular2TestModule.ANGULAR_COMMON_4_0_0, extension = "html")

  fun testTemplatesCompletion16() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8, extension = "html",
                 configurators = listOf(Angular2TsConfigFile(strict = false)))

  fun testTemplatesCompletion16Strict() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8,
                 extension = "html", configurators = listOf(Angular2TsConfigFile()))

  fun testPrimaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testPrimaryBlocksTopLevelText() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", lookupItemFilter = ::notAnElement)

  fun testPrimaryBlocksNestedText() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", lookupItemFilter = ::notAnElement)

  fun testIfSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testIfSecondaryBlocksNested() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testIfSecondaryBlocksTopLevelText() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", lookupItemFilter = ::notAnElement)

  fun testIfSecondaryBlocksNestedText() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", lookupItemFilter = ::notAnElement)

  fun testAfterElseBlock() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testDeferSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testDeferSecondaryBlocksUnique() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testForSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testForSecondaryBlocksNested() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testForSecondaryBlocksUnique() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testSwitchSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testSwitchNoSiblingSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testSwitchSecondaryBlocksUnique() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testIfBlock() =
    doTypingTest("if\n", Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testErrorBlock() =
    doTypingTest("err\n", Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testTagsWithinBlock() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testTagsWithinBlock2() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testNoGlobalImportInTsFiles() =
    doTypingTest("do1\n", dir = true)

  fun testIfBlockParameters() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", checkDocumentation = true)

  fun testIfBlockParameterTyping() =
    doTypingTest("\nfoo", Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testForBlockOfKeyword() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testForBlockParams() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", checkDocumentation = true)

  fun testForBlockLetEqKeyword() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testForBlockImplicitVariables() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testForBlockImplicitVariableInExpr() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", checkDocumentation = true,
                 lookupItemFilter = { it.lookupString == "\$count" })

  fun testDeferBlockTimeExpressionStart() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testDeferBlockTimeExpressionTimeUnit() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testDeferBlockParameters() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testDeferBlockParametersNg19() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_19_0_0_NEXT_4, extension = "html")

  fun testDeferBlockPrefetchParameters() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testDeferBlockHydrateParameters() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_19_0_0_NEXT_4, extension = "html")

  fun testDeferBlockOnTriggers() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testDeferBlockOnTimerNoArgCompletion() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testDeferBlockOnViewport() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testKeyofAttribute() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, configurators = listOf(Angular2TsConfigFile()))

  fun testTsconfigPriority() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html", dir = true, configureFileName = "src/component.html") {
      it.priority >= 100
    }

  fun testStructuralDirectiveWithInjectedFields() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testLetBlockTemplate() {
    TemplateManagerImpl.setTemplateTesting(testRootDisposable)
    doTypingTest("le\nfoo\t12\n", Angular2TestModule.ANGULAR_CORE_18_2_1, extension = "html")
  }

  fun testLetBlockExpression() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_18_2_1, extension = "html") {
      it.priority >= 100
    }

  fun testHostBindingJSProperty1() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts") {
      it.lookupString.startsWith("ti")
      || it.lookupString.startsWith("[s")
      || it.lookupString.startsWith("(cl")
    }

  fun testHostBindingJSProperty2() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  fun testHostBindingCssClass1() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  fun testHostBindingCssClass2() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  fun testHostBindingDecorator1() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  fun testHostBindingDecorator2() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  fun testHostListenerDecorator() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  fun testObjectInitializerProperties() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts",
                 locations = listOf("[product]=\"{<caret>}\"", "[product]=\"{title,<caret>}\""))

  fun testViewChildrenDecorator() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts",
                 locations = listOf("@ViewChild('<caret>area')", "@ViewChildren('<caret>area')"))

  fun testViewChildrenDecoratorHtml() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts", dir = true,
                 locations = listOf("@ViewChild('<caret>area')", "@ViewChildren('<caret>area')"))

  fun testViewChildrenSignal() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts",
                 locations = (1..6).map { "\"<caret>area$it\"" })

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

  fun testTemplateBindingsNgForContextDocumentation() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1, extension = "ts",
                 lookupItemFilter = { it.lookupString == "index" || it.lookupString == "last" || it.lookupString == "ngForOf"},
                 checkDocumentation = true)

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