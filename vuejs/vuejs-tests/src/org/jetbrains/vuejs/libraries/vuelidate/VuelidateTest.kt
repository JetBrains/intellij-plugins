// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuelidate

import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.polySymbols.testFramework.moveToOffsetBySignature
import com.intellij.polySymbols.testFramework.renderLookupItems
import com.intellij.polySymbols.testFramework.resolveReference
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VuelidateTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/libraries/vuelidate")
  private val myFixture get() = hybrid.fixture

  @BeforeEach
  fun setUp() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10, VueTestModule.VUELIDATE_0_7_13)
    myFixture.createFile("imports.ts", "import 'vuelidate/vue'")
  }

  @Test
  fun testCompletion(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile("basic.vue")
    myFixture.moveToOffsetBySignature("v-if=\"!\$v.<caret>")
    myFixture.completeBasic()
    assertThat(myFixture.renderLookupItems(true, true)).contains(
                           "typed (typeText=null; priority=101.0; bold)",
                           "name (typeText=null; priority=101.0; bold)",
                           "age (typeText=null; priority=101.0; bold)")

    myFixture.moveToOffsetBySignature("{{\$v.<caret>")
    myFixture.completeBasic()
    assertThat(myFixture.renderLookupItems(true, true)).contains(
                           "typed (typeText=null; priority=101.0; bold)",
                           "name (typeText=null; priority=101.0; bold)",
                           "age (typeText=null; priority=101.0; bold)")


    myFixture.moveToOffsetBySignature("this.\$v.name.\$<caret>")
    myFixture.completeBasic()
    assertThat(myFixture.renderLookupItems(true, true)).contains(
                           "\$touch (typeText='void'; priority=101.0; bold)",
                           "\$error (typeText='boolean'; priority=101.0; bold)",
                           "\$invalid (typeText='boolean'; priority=101.0; bold)")
  }

  @Test
  fun testResolve(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile("basic.vue")
    assertEquals("age: 0", myFixture.resolveReference("{{\$v.a<caret>ge.\$params").parent.context?.text)
    assertEquals("name: ''", myFixture.resolveReference("!\$v.na<caret>me.required").parent.context?.text)
    assertEquals("name: ''", myFixture.resolveReference("this.\$v.n<caret>ame.\$touch()").parent.context?.text)
  }

  @Test
  fun testHighlighting(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureByFile("highlighting.vue")
    myFixture.checkHighlighting()
  }
}