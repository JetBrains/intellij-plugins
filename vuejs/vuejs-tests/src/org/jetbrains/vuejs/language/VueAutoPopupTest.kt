// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.language

import com.intellij.codeInsight.completion.CompletionAutoPopupTestCase
import junit.framework.TestCase

class VueAutoPopupTest : CompletionAutoPopupTestCase() {
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
    myFixture.assertPreferredCompletionItems(0, ":about", ":accesskey", ":align", ":aria-activedescendant")
  }

  fun testNoAutopopupAfterMinus() {
    myFixture.configureByText("a.vue", "<div <caret>>")
    type("-")
    TestCase.assertNull(myFixture.lookup)
  }
}
