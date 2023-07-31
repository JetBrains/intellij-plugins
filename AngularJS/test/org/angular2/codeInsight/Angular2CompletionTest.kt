// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.webSymbols.enableIdempotenceChecksOnEveryCache
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule.ANGULAR_CDK_14_2_0
import org.angular2.Angular2TestModule.ANGULAR_CORE_13_3_5

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

  fun testHostDirectiveOutputMapping() =
    doLookupTest(ANGULAR_CORE_13_3_5, renderTypeText = true, renderPriority = false)

  fun testHostDirectiveInputMappingWithReplace() {
    doConfiguredTest(checkResult = true) {
      completeBasic()
      type("vir\t")
    }
  }

  fun testCompletionInExpression() {
    doLookupTest(ANGULAR_CORE_13_3_5, ANGULAR_CDK_14_2_0, dir = true)

    // Export from other file
    myFixture.type("kThemes\n")
    myFixture.type(".")
    myFixture.completeBasic()
    myFixture.type("l\n;")

    // Local symbol
    myFixture.type("CdkColors")
    myFixture.completeBasic()
    myFixture.type(".")
    myFixture.completeBasic()
    myFixture.type("re\n;")

    // Global symbol
    myFixture.type("Ma")
    myFixture.completeBasic()
    myFixture.type("th\n")
    myFixture.type(".")
    myFixture.completeBasic()
    myFixture.type("abs\n")

    myFixture.checkResultByFile("completionInExpression/completionInExpression.ts.after")
  }

}