// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuelidate

import com.intellij.javascript.web.moveToOffsetBySignature
import com.intellij.javascript.web.renderLookupItems
import com.intellij.javascript.web.resolveReference
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.*

class VuelidateTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/vuelidate"

  override fun setUp() {
    super.setUp()
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10, VueTestModule.VUELIDATE_0_7_13)
  }

  fun testCompletion() {
    myFixture.configureByFile("basic.vue")
    myFixture.moveToOffsetBySignature("v-if=\"!\$v.<caret>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.renderLookupItems(true, true),
                           "!typed#null#101", "!name#null#101", "!age#null#101")

    myFixture.moveToOffsetBySignature("{{\$v.<caret>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.renderLookupItems(true, true),
                           "!typed#null#101", "!name#null#101", "!age#null#101")


    myFixture.moveToOffsetBySignature("this.\$v.name.\$<caret>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.renderLookupItems(true, true),
                           "!\$touch#void#101", "!\$error#boolean#101", "!\$invalid#boolean#101")
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