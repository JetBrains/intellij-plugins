// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.polySymbols.testFramework.enableIdempotenceChecksOnEveryCache
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule.ANGULAR_CORE_17_3_0
import org.angular2.TestNoService
import org.junit.Test

@TestNoService
class Angular2AttributeNameCompletionTest : Angular2TestCase("completion/attributeName") {

  override fun setUp() {
    super.setUp()
    enableIdempotenceChecksOnEveryCache()
  }

  @Test
  fun testEventWithExistingValue() =
    doTypingTest("d\n")

  @Test
  fun testEventWithoutValue() =
    doTypingTest("ck\n")

  @Test
  fun testModifierWithExistingValue() =
    doEditorTypingTest(dir = true, extension = "html", checkResult = false,
                       after = { checkResultByFile("$testName/${testName}_after.html") }) {
      completeBasic()
      type("sto\n")
    }

  @Test
  fun testKeyEventModifiersWithExistingValue() =
    doTypingTest("al\n", "ent\n")

  @Test
  fun testKeyEventModifierBeforeExistingModifier() =
    doTypingTest("al\n")

  @Test
  fun testCanonicalKeyEventWithExistingValue() =
    doTypingTest("al\n", "ent\n")

  @Test
  fun testAttributeBindingWithExistingValue() =
    doTypingTest("aria-la\n")

  private fun doTypingTest(vararg toType: String) =
    doEditorTypingTest(ANGULAR_CORE_17_3_0, extension = "html") {
      completeBasic()
      toType.forEach { type(it) }
    }
}
