// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuelidate

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import com.intellij.webSymbols.testFramework.renderLookupItems
import com.intellij.webSymbols.testFramework.resolveReference
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath

class VuelidateTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/vuelidate"

  override fun setUp() {
    super.setUp()
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10, VueTestModule.VUELIDATE_0_7_13)
    myFixture.createFile("imports.ts", "import 'vuelidate/vue'")
  }

  fun testCompletion() {
    myFixture.configureByFile("basic.vue")
    myFixture.moveToOffsetBySignature("v-if=\"!\$v.<caret>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.renderLookupItems(true, true),
                           "typed (typeText=null; priority=101.0; bold)",
                           "name (typeText=null; priority=101.0; bold)",
                           "age (typeText=null; priority=101.0; bold)")

    myFixture.moveToOffsetBySignature("{{\$v.<caret>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.renderLookupItems(true, true),
                           "typed (typeText=null; priority=101.0; bold)",
                           "name (typeText=null; priority=101.0; bold)",
                           "age (typeText=null; priority=101.0; bold)")


    myFixture.moveToOffsetBySignature("this.\$v.name.\$<caret>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.renderLookupItems(true, true),
                           "\$touch (typeText='void'; priority=101.0; bold)",
                           "\$error (typeText='boolean'; priority=101.0; bold)",
                           "\$invalid (typeText='boolean'; priority=101.0; bold)")
  }

  fun testResolve() {
    myFixture.configureByFile("basic.vue")
    TestCase.assertEquals("age: 0", myFixture.resolveReference("{{\$v.a<caret>ge.\$params").context?.text)
    TestCase.assertEquals("name: ''", myFixture.resolveReference("!\$v.na<caret>me.required").context?.text)
    TestCase.assertEquals("name: ''", myFixture.resolveReference("this.\$v.n<caret>ame.\$touch()").context?.text)
  }

  fun testHighlighting() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureByFile("highlighting.vue")
    myFixture.checkHighlighting()
  }
}