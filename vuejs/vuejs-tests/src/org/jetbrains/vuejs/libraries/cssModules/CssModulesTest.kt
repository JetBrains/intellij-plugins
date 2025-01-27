// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.cssModules

import com.intellij.webSymbols.testFramework.assertUnresolvedReference
import com.intellij.javascript.testFramework.web.checkUsages
import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import com.intellij.webSymbols.testFramework.resolveReference
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.ComparisonFailure
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.*

class CssModulesTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/cssModules"

  fun testRootCompletion() {
    myFixture.configureByFiles("cssModule.vue")
    myFixture.moveToOffsetBySignature("{{ <caret>\$style")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "\$style", "myStyle", "theStyle")
    assertDoesntContain(myFixture.lookupElementStrings!!, "noStyle")
  }

  fun testStyleCompletion() {
    myFixture.configureByFiles("cssModule.vue")
    myFixture.moveToOffsetBySignature("{{ \$style.<caret>")
    myFixture.completeBasic()
    TestCase.assertEquals(listOf("bar", "constructor", "foo", "hasOwnProperty", "isPrototypeOf", "la", "la2", "la3", "local4",
                                 "propertyIsEnumerable", "toLocaleString", "toString", "valueOf"),
                          myFixture.lookupElementStrings!!.sorted())
  }

  fun testMyStyleCompletion() {
    myFixture.configureByFiles("cssModule.vue")
    myFixture.moveToOffsetBySignature("{{ myStyle.<caret>")
    myFixture.completeBasic()
    TestCase.assertEquals(listOf("\"my-stuff\"", "constructor", "hasOwnProperty", "isPrototypeOf", "propertyIsEnumerable",
                                 "toLocaleString", "toString", "valueOf"), myFixture.lookupElementStrings!!.sorted())
  }

  fun testHighlighting() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureByFiles("cssModule.highlighting.vue")
    myFixture.checkHighlighting()
  }

  fun testResolve() {
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
          TestCase.assertEquals(result, myFixture.resolveReference(signature).context?.context?.text)
        }
        else {
          myFixture.assertUnresolvedReference(signature, true)
        }
      }
      catch (e: ComparisonFailure) {
        throw ComparisonFailure(signature + ":" + e.message, e.expected, e.actual).initCause(e)
      }
      catch (e: AssertionError) {
        throw AssertionError(signature + ":" + e.message, e)
      }
    }
  }

  fun testUsages() {
    myFixture.configureByFile("cssModule.vue")
    myFixture.checkUsages(".b<caret>ar", "bar-usages")
  }

}
