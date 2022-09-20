// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.webSymbols.assertUnresolvedReference
import com.intellij.webSymbols.moveToOffsetBySignature
import com.intellij.webSymbols.resolveReference
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.util.Arrays.asList

class VueCssClassTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/cssClass"

  fun testNgClassCodeCompletion() {
    myFixture.configureByFiles("basic.vue")
    for (prefix in asList("{", "[", "")) {
      myFixture.moveToOffsetBySignature("=\"$prefix'foo1 b<caret>'")
      myFixture.completeBasic()
      UsefulTestCase.assertSameElements(myFixture.lookupElementStrings!!, "bar", "boo")
    }
    myFixture.moveToOffsetBySignature(", foo1: true<caret>}\"")
    myFixture.type(",")
    myFixture.completeBasic()

    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "bar", "boo", "foo")
  }

  fun testNgClassReferences() {
    myFixture.configureByFiles("basic.vue")
    for (prefix in asList("{", "[", "")) {
      myFixture.resolveReference("=\"$prefix'fo<caret>o b")
      myFixture.resolveReference("=\"$prefix'foo b<caret>ar")
      myFixture.assertUnresolvedReference("=\"$prefix'f<caret>oo1 ")
      myFixture.assertUnresolvedReference("=\"$prefix'foo1 b<caret>")
    }
    myFixture.resolveReference(", b<caret>ar: true}\"")
    myFixture.assertUnresolvedReference(", f<caret>oo1: true}\"")
  }

}
