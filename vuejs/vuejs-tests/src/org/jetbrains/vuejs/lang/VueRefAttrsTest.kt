// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.web.moveToOffsetBySignature
import com.intellij.javascript.web.resolveReference
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.ComparisonFailure
import junit.framework.TestCase

class VueRefAttrsTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/refAttrs"

  fun testJSCompletionAndRename() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.configureByFile("js.vue")

    myFixture.moveToOffsetBySignature(" \$refs.<caret>")
    myFixture.completeBasic()
    myFixture.type("inpu\n[0].")
    myFixture.completeBasic()
    myFixture.type("valida\n")

    myFixture.moveToOffsetBySignature("this.\$refs.<caret>")
    myFixture.completeBasic()
    myFixture.type("abo\n.")
    myFixture.completeBasic()
    myFixture.type("\$re\n")

    myFixture.type("\nthis.\$refs.divRef")
    myFixture.moveToOffsetBySignature("this.\$refs.div<caret>Ref")
    myFixture.renameElementAtCaret("divRef3")

    myFixture.checkResultByFile("js.after.vue")
  }

  fun testTSCompletionAndRename() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.configureByFile("ts.vue")

    myFixture.moveToOffsetBySignature(" \$refs.<caret>")
    myFixture.completeBasic()
    myFixture.type("inpu\n[0].")
    myFixture.completeBasic()
    myFixture.type("valida\n")

    myFixture.moveToOffsetBySignature("this.\$refs.<caret>")
    myFixture.completeBasic()
    myFixture.type("abo\n.")
    myFixture.completeBasic()
    myFixture.type("\$re\n")

    myFixture.type("\nthis.\$refs.divRef")
    myFixture.moveToOffsetBySignature("this.\$refs.div<caret>Ref")
    myFixture.renameElementAtCaret("divRef3")

    myFixture.checkResultByFile("ts.after.vue")
  }

  fun testResolve() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.configureByFile("js.after.vue")
    for ((signature, result) in listOf(
      Pair("\$refs.input<caret>Ref", "ref='inputRef'"),
      Pair("\$refs.inputRef[0].validation<caret>Message",
           "/** Returns the error message that would be displayed if the user submits the form, or an empty string if no error message. " +
           "It also triggers the standard error message, such as \"this is a required field\". " +
           "The result is that the user sees validation messages without actually submitting. */\n" +
           "    readonly validationMessage: string"),
      Pair("this.\$refs.about.\$re<caret>fs", "\$refs: Data"),
      Pair("this.\$refs.div<caret>Ref3", "ref='divRef3'"),
      Pair("this.\$refs.di<caret>v\n", "{\n    [P in K]: T;\n}"),
    )) {
      try {
        TestCase.assertEquals(result, myFixture.resolveReference(signature).let { if (it is JSImplicitElement) it.context!! else it }.text)
      }
      catch (e: ComparisonFailure) {
        throw ComparisonFailure(signature + ":" + e.message, e.expected, e.actual).initCause(e)
      }
      catch (e: AssertionError) {
        throw AssertionError(signature + ":" + e.message, e)
      }
    }
  }

  fun testHighlighting() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureByFile("js.highlighting.vue")
    myFixture.checkHighlighting()
  }

}