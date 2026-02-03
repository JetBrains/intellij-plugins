// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.javascript.testFramework.web.WebFrameworkTestConfigurator
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroAttributeInsertHandlerTest : AstroCodeInsightTestCase("codeInsight/completion/attributeInsertHandler") {

  fun testClassListDirectiveInsertsBraces() =
    doTypingTest("")

  fun testSetHtmlDirectiveInsertsBraces() =
    doTypingTest("set:html\n")

  fun testSetTextDirectiveInsertsBraces() =
    doTypingTest("set:text\n")

  fun testDefineVarsDirectiveInsertsBraces() =
    doTypingTest("define:vars\n")

  fun testClientLoadDirectiveOnComponent() =
    doTypingTest("client:load", additionalFiles = listOf("MyComponent.astro"))

  fun testClassAttributeInsertsQuotes() =
    doTypingTest("class\n")

  fun testStyleAttributeInsertsQuotes() =
    doTypingTest("style\n")

  fun testAstroComponentPropStringTypeInsertsQuotes() =
    doTypingTest("title\n", additionalFiles = listOf("component.astro"))

  fun testAstroComponentPropNumber() =
    doTypingTest("ve\n", additionalFiles = listOf("component.astro"))

  fun testAstroComponentPropString() =
    doTypingTest("size\n", additionalFiles = listOf("component.astro"))

  fun testAstroComponentPropBoolean() =
    doTypingTest("ena\n", additionalFiles = listOf("component.astro"))

  //region Helper methods

  private fun doTypingTest(
    textToType: String,
    additionalFiles: List<String> = emptyList(),
    configurators: List<WebFrameworkTestConfigurator> = emptyList(),
  ) {
    doConfiguredTest(additionalFiles = additionalFiles, configurators = configurators, checkResult = true) {
      completeBasic()
      type(textToType)
    }
  }

  //endregion
}