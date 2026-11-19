// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.cssModules

import com.intellij.javascript.testFramework.web.checkJsUsages
import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.assertUnresolvedReference
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.polySymbols.testFramework.moveToOffsetBySignature
import com.intellij.polySymbols.testFramework.resolveReference
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class CssModulesTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/libraries/cssModules")
  private val myFixture get() = hybrid.fixture

  @Test
  fun testRootCompletion(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFiles("cssModule.vue")
    myFixture.moveToOffsetBySignature("{{ <caret>\$style")
    myFixture.completeBasic()
    assertThat(myFixture.lookupElementStrings!!).contains("\$style", "myStyle", "theStyle")
    assertThat(myFixture.lookupElementStrings!!).doesNotContain("noStyle")
  }

  @Test
  fun testStyleCompletion(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFiles("cssModule.vue")
    myFixture.moveToOffsetBySignature("{{ \$style.<caret>")
    myFixture.completeBasic()
    assertEquals(listOf("bar", "constructor", "foo", "hasOwnProperty", "isPrototypeOf", "la", "la2", "la3", "local4",
                                 "propertyIsEnumerable", "toLocaleString", "toString", "valueOf"),
                          myFixture.lookupElementStrings!!.sorted())
  }

  @Test
  fun testMyStyleCompletion(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFiles("cssModule.vue")
    myFixture.moveToOffsetBySignature("{{ myStyle.<caret>")
    myFixture.completeBasic()
    assertEquals(listOf("\"my-stuff\"", "constructor", "hasOwnProperty", "isPrototypeOf", "propertyIsEnumerable",
                        "toLocaleString", "toString", "valueOf"), myFixture.lookupElementStrings!!.sorted())
  }

  @Test
  fun testHighlighting(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureByFiles("cssModule.highlighting.vue")
    myFixture.checkHighlighting()
  }

  @Test
  fun testResolve(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile("cssModule.vue")

    myFixture.resolveReference("\$st<caret>yle.")
    myFixture.resolveReference("my<caret>Style.")
    myFixture.resolveReference("the<caret>Style.")

    for ((signature, result) in listOf(
      Pair("no<caret>Style", null),
      Pair("\$style.fo<caret>o", ".foo"),
      Pair("\$style.glo<caret>b1", null),
      Pair("\$style.l<caret>a", ".la"),
      Pair("myStyle.fo<caret>o", null),
      Pair("theStyle.the_<caret>_stuff", ".the__stuff"),
      Pair("noStyle.f<caret>oo", null),
    )) {
      try {
        if (result != null) {
          assertEquals(result, myFixture.resolveReference(signature).context?.context?.text)
        }
        else {
          myFixture.assertUnresolvedReference(signature, true)
        }
      }
      catch (e: AssertionError) {
        throw AssertionError(signature + ":" + e.message, e)
      }
    }
  }

  @Test
  fun testUsages(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile("cssModule.vue")
    myFixture.checkJsUsages(".b<caret>ar", "bar-usages")
  }

}
