// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.psi.css.CssPseudoClass
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.webSymbols.testFramework.assertUnresolvedReference
import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import com.intellij.webSymbols.testFramework.resolveReference
import java.util.Arrays.asList

class VueCssResolveTest : BasePlatformTestCase() {

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

  fun testPseudoWithSelector() {
    myFixture.configureByFiles(getTestName(true) + ".vue")

    assertEquals(":deep(.foo > .bar)",
                 myFixture.resolveReference("class=\"<caret>bar\"").parentOfType<CssPseudoClass>()?.text)

    assertEquals(":deep(.foo)",
                 myFixture.resolveReference("class=\"<caret>foo\"").parentOfType<CssPseudoClass>()?.text)

    // resolves to the attribute itself instead of a css class, consistent with the current behavior WEB-279
    assertInstanceOf(myFixture.resolveReference("class=\"<caret>background\""), XmlAttributeValue::class.java)
  }

}
