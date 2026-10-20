// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.junit.jupiter.api.Test

@TestApplication
class AstroAttributeInsertHandlerTest : AstroCodeInsightTestCase("codeInsight/completion/attributeInsertHandler") {

  @Test
  fun testClassListDirectiveInsertsBraces() =
    doTypingTest("")

  @Test
  fun testSetHtmlDirectiveInsertsBraces() =
    doTypingTest("set:html\n")

  @Test
  fun testSetTextDirectiveInsertsBraces() =
    doTypingTest("set:text\n")

  @Test
  fun testDefineVarsDirectiveInsertsBraces() =
    doTypingTest("define:vars\n")

  @Test
  fun testClientLoadDirectiveOnComponent() =
    doTypingTest("client:load", additionalFiles = listOf("MyComponent.astro"))

  @Test
  fun testClassAttributeInsertsQuotes() =
    doTypingTest("class\n")

  @Test
  fun testStyleAttributeInsertsQuotes() =
    doTypingTest("style\n")

  @Test
  fun testAstroComponentPropStringTypeInsertsQuotes() =
    doTypingTest("title\n", additionalFiles = listOf("component.astro"))

  @Test
  fun testAstroComponentPropNumber() =
    doTypingTest("ve\n", additionalFiles = listOf("component.astro"))

  @Test
  fun testAstroComponentPropString() =
    doTypingTest("size\n", additionalFiles = listOf("component.astro"))

  @Test
  fun testAstroComponentPropBoolean() =
    doTypingTest("ena\n", additionalFiles = listOf("component.astro"))

  //region Helper methods

  private fun doTypingTest(
    textToType: String,
    additionalFiles: List<String> = emptyList(),
    configurators: List<PolySymbolsTestConfigurator> = emptyList(),
  ) {
    astro.doEditorTypingTest(additionalFiles = additionalFiles, configurators = configurators, checkResult = true) {
      completeBasic()
      type(textToType)
    }
  }

  //endregion
}