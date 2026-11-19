// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.assertUnresolvedReference
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.polySymbols.testFramework.moveToOffsetBySignature
import com.intellij.polySymbols.testFramework.resolveReference
import com.intellij.psi.css.CssPseudoClass
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.util.Arrays.asList
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VueCssResolveTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/cssClass")
  private val myFixture get() = hybrid.fixture

  @Test
  fun testNgClassCodeCompletion(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFiles("basic.vue")
    for (prefix in asList("{", "[", "")) {
      myFixture.moveToOffsetBySignature("=\"$prefix'foo1 b<caret>'")
      myFixture.completeBasic()
      assertThat(myFixture.lookupElementStrings!!).containsExactlyInAnyOrder("bar", "boo")
    }
    myFixture.moveToOffsetBySignature(", foo1: true<caret>}\"")
    myFixture.type(",")
    myFixture.completeBasic()

    assertThat(myFixture.lookupElementStrings!!).contains("bar", "boo", "foo")
  }

  @Test
  fun testNgClassReferences(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
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

  @Test
  fun testPseudoWithSelector(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFiles(hybrid.getTestName(true) + ".vue")

    assertEquals(":deep(.foo > .bar)",
                 myFixture.resolveReference("class=\"<caret>bar\"").parentOfType<CssPseudoClass>()?.text)

    assertEquals(":deep(.foo)",
                 myFixture.resolveReference("class=\"<caret>foo\"").parentOfType<CssPseudoClass>()?.text)

    // resolves to the attribute itself instead of a css class, consistent with the current behavior WEB-279
    assertInstanceOf(XmlAttributeValue::class.java, myFixture.resolveReference("class=\"<caret>background\""))
  }

}
