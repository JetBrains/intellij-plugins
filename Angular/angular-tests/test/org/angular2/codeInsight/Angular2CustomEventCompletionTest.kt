// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.polySymbols.testFramework.enableIdempotenceChecksOnEveryCache
import org.angular2.Angular2TestCase
import org.angular2.TestTsGoProxy
import org.angular2.TestTsNode
import org.junit.Test

@TestTsNode
@TestTsGoProxy
class Angular2CustomEventCompletionTest : Angular2TestCase("completion/customEvents") {

  override fun setUp() {
    super.setUp()
    enableIdempotenceChecksOnEveryCache()
  }

  @Test
  fun testEventWithExistingValue() =
    doConfiguredTest(dir = true, configureFileName = "eventWithExistingValue.html") {
      completeBasic()
      type("d\n")
      checkResultByFile("$testName/eventWithExistingValue.after.html")
    }

  @Test
  fun testModifierWithExistingValue() =
    doConfiguredTest(dir = true, configureFileName = "modifierWithExistingValue.html") {
      completeBasic()
      type("sto\n")
      checkResultByFile("$testName/modifierWithExistingValue.after.html")
    }
}
