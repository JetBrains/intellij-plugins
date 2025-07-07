// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import org.jetbrains.vuejs.VueTestCase

class VueAutoPopupTest : VueTestCase("autoPopup") {

  fun testEventsAfterAt() =
    doCompletionAutoPopupTest(checkResult = false) {
      type("@")
      checkLookupItems { item ->
        item.lookupString.let {
          it.startsWith("@a") || it.startsWith("@b") || it.startsWith("@c")
        }
      }
    }

  fun testEventsAfterVOnColon() =
    doCompletionAutoPopupTest(checkResult = false) {
      type(":")
      checkLookupItems { item ->
        item.lookupString.let {
          it.startsWith("a") || it.startsWith("b") || it.startsWith("c")
        }
      }
    }

  fun testVBindShorthand() =
    doCompletionAutoPopupTest(checkResult = false) {
      type(":")
      assertLookupShown()
      type("a")
      checkLookupItems{ item ->
        item.lookupString.let {
          !it.startsWith(":aria-") || it.startsWith(":aria-a")
        }
      }
    }

  fun testNoAutopopupAfterMinus() =
    doCompletionAutoPopupTest(checkResult = false) {
      type("-")
      assertLookupNotShown()
    }
}
