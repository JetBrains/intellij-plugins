// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.VueFixture
import org.jetbrains.vuejs.VueTestMode
import org.jetbrains.vuejs.vueFixture
import org.junit.jupiter.api.Test

@TestApplication
class VueAutoPopupTest :
  VueAutoPopupTestBase() {

  @TestApplication
  class WithTsGoProxyTest :
    VueAutoPopupTestBase(testMode = VueTestMode.TS_GO_PROXY)

  @TestApplication
  class WithoutServiceTest :
    VueAutoPopupTestBase(testMode = VueTestMode.NO_PLUGIN)
}

abstract class VueAutoPopupTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) {

  protected val vue: VueFixture by vueFixture("autoPopup", testMode = testMode)

  @Test
  fun testEventsAfterAt() =
    vue.doEditorTypingTest(checkResult = false) {
      type("@")
      checkLookupItems { item ->
        item.lookupString.let {
          it.startsWith("@a") || it.startsWith("@b") || it.startsWith("@c")
        }
      }
    }

  @Test
  fun testEventsAfterVOnColon() =
    vue.doEditorTypingTest(checkResult = false) {
      type(":")
      checkLookupItems { item ->
        item.lookupString.let {
          it.startsWith("a") || it.startsWith("b") || it.startsWith("c")
        }
      }
    }

  @Test
  fun testVBindShorthand() =
    vue.doEditorTypingTest(checkResult = false) {
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
    vue.doEditorTypingTest(checkResult = false) {
      type("-")
      assertLookupNotShown()
    }
}
