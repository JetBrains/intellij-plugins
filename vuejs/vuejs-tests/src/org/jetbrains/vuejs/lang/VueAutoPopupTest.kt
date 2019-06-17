// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSAutoPopupTestBase
import junit.framework.TestCase

class VueAutoPopupTest : JSAutoPopupTestBase() {
  fun testEventsAfterAt() {
    myFixture.configureByText("a.vue", "<div <caret>>")
    type("@")
    myFixture.assertPreferredCompletionItems(0, "@abort", "@autocomplete", "@autocompleteerror", "@blur", "@cancel", "@canplay")
  }

  fun testEventsAfterVOnColon() {
    myFixture.configureByText("a.vue", "<div v-on<caret>>")
    type(":")
    myFixture.assertPreferredCompletionItems(0, "abort", "autocomplete", "autocompleteerror", "blur", "cancel", "canplay")
  }

  fun testVBindShorthand() {
    myFixture.configureByText("a.vue", "<div <caret>>")
    type(":")
    TestCase.assertNotNull(myFixture.lookup)
    type("a")
    myFixture.assertPreferredCompletionItems(0, ":about", ":accesskey", ":align", ":aria-activedescendant")
  }

  fun testNoAutopopupAfterMinus() {
    myFixture.configureByText("a.vue", "<div <caret>>")
    type("-")
    TestCase.assertNull(myFixture.lookup)
  }
}
