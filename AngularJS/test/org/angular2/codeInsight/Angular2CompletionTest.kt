// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.javascript.web.WebFrameworkTestModule
import com.intellij.webSymbols.LookupElementInfo
import com.intellij.webSymbols.enableIdempotenceChecksOnEveryCache
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.ANGULAR_CORE_13_3_5
import org.angular2.Angular2TsConfigFile

class Angular2CompletionTest : Angular2TestCase("completion") {

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

  fun testCustomSignal() =
    doBasicCompletionTest(Angular2TestModule.ANGULAR_CORE_16_2_8)

  fun testSignalInGenericStructuralDirective() =
    doBasicCompletionTest(Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8,
                          dir = true, extension = "html")

  fun testSignalInGenericDirective() =
    doBasicCompletionTest(Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8,
                          dir = true, extension = "html")

  fun testNotSignal() =
    doBasicCompletionTest(Angular2TestModule.ANGULAR_CORE_16_2_8)

  fun testTemplatesCompletion() =
    doLookupTest(Angular2TestModule.ANGULAR_COMMON_4_0_0, extension = "html")

  fun testTemplatesCompletion16() =
    doLookupTest(Angular2TestModule.ANGULAR_COMMON_16_2_8, extension = "html")

  fun testTemplatesCompletion16Strict() =
    doLookupTest(Angular2TestModule.ANGULAR_COMMON_16_2_8, extension = "html", configurators = listOf(Angular2TsConfigFile()))

  fun testPrimaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testPrimaryBlocksTopLevelText() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html", lookupItemFilter = ::notAnElement)

  fun testPrimaryBlocksNestedText() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html", lookupItemFilter = ::notAnElement)

  fun testIfSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testIfSecondaryBlocksNested() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testIfSecondaryBlocksTopLevelText() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html", lookupItemFilter = ::notAnElement)

  fun testIfSecondaryBlocksNestedText() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html", lookupItemFilter = ::notAnElement)

  fun testAfterElseBlock() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testDeferSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testDeferSecondaryBlocksUnique() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testForSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testForSecondaryBlocksNested() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testForSecondaryBlocksUnique() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testSwitchSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testSwitchNoSiblingSecondaryBlocks() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testSwitchSecondaryBlocksUnique() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testIfBlock() =
    doTypingTest("if\n", Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0)

  fun testErrorBlock() =
    doTypingTest("err\n", Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0)

  fun testTagsWithinBlock() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testTagsWithinBlock2() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testNoGlobalImportInTsFiles() =
    doTypingTest("do1\n", dir = true)

  fun testIfBlockParameters() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html", checkDocumentation = true)

  fun testIfBlockParameterTyping() =
    doTypingTest("\nfoo", Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0)

  fun testForBlockOfKeyword() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testForBlockParams() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html", checkDocumentation = true)

  fun testForBlockLetEqKeyword() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testForBlockImplicitVariables() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testForBlockImplicitVariableInExpr() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html", checkDocumentation = true,
                 lookupItemFilter = { it.lookupString == "\$count" })

  fun testDeferBlockTimeExpressionStart() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testDeferBlockTimeExpressionTimeUnit() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testDeferBlockParameters() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testDeferBlockPrefetchParameters() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testDeferBlockOnTriggers() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testDeferBlockOnTimerNoArgCompletion() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  fun testDeferBlockOnViewport() =
    doLookupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

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