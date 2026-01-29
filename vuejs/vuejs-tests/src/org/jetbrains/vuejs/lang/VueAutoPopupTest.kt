// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.VueTestMode
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

class VueAutoPopupTest :
  VueAutoPopupTestBase() {

  class WithLegacyPluginTest :
    VueAutoPopupTestBase(testMode = VueTestMode.LEGACY_PLUGIN)

  class WithoutServiceTest :
    VueAutoPopupTestBase(testMode = VueTestMode.NO_PLUGIN)
}

@RunWith(JUnit4::class)
abstract class VueAutoPopupTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) : VueTestCase("autoPopup", testMode = testMode) {

  @Test
  fun testEventsAfterAt() =
    doCompletionAutoPopupTest(checkResult = false) {
      type("@")
      checkLookupItems { item ->
        item.lookupString.let {
          it.startsWith("@a") || it.startsWith("@b") || it.startsWith("@c")
        }
      }
    }

  @Test
  fun testEventsAfterVOnColon() =
    doCompletionAutoPopupTest(checkResult = false) {
      type(":")
      checkLookupItems { item ->
        item.lookupString.let {
          it.startsWith("a") || it.startsWith("b") || it.startsWith("c")
        }
      }
    }

  @Test
  fun testVBindShorthand() =
    doCompletionAutoPopupTest(checkResult = false) {
      type(":")
      assertLookupShown()
      type("a")
      checkLookupItems { item ->
        item.lookupString.let {
          !it.startsWith(":aria-") || it.startsWith(":aria-a")
        }
      }
    }

  @Test
  fun testNoAutopopupAfterMinus() =
    doCompletionAutoPopupTest(checkResult = false) {
      type("-")
      assertLookupNotShown()
    }
}
