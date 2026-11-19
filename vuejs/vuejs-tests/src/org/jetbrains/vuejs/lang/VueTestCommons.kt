// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.index.VueIndexData
import org.jetbrains.vuejs.index.getVueIndexData
import org.jetbrains.vuejs.index.serializeUserStringData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class VueTestCommons {

  @Test
  fun testFromAsset() {
    for (test in listOf(
      "foo" to "foo",
      "fooBar" to "foo-bar",
      "fooBarFoo" to "foo-bar-foo",
      "FOO" to "f-o-o",
      "FOOBar" to "f-o-o-bar",
      "fooBAR" to "foo-b-a-r",
      "fooBARFoo" to "foo-b-a-r-foo",
      "URL20Converter" to "u-r-l20-converter",
      "URL20converter" to "u-r-l20converter"
    )) {
      assertEquals(test.second, fromAsset(test.first), test.first)
    }
  }

  @Test
  fun testToAsset() {
    for (test in listOf(
      "foo" to "foo",
      "foo-bar" to "fooBar",
      "foo-bar-foo" to "fooBarFoo",
      "url20-converter" to "url20Converter",
      "url20converter" to "url20converter",
      "foo-b-a-r-foo" to "fooBARFoo"
    )) {
      assertEquals(test.second, toAsset(test.first), test.first)
    }
  }

  /**
   * Convert VueIndexData into String and back
   */
  @Test
  fun testIndexDataSerialization() {
    doTestSerialization(VueIndexData("""""", """""", """""", false, false))
    doTestSerialization(VueIndexData("""Hello""", """hello.world""", """hello.world""", false, true))
    doTestSerialization(VueIndexData("""#Hello""", """hello.#world""", """hello.#world""", false, true))
    doTestSerialization(VueIndexData("""\Hello""", """hello.\world""", """hello.\world""", false, true))
    doTestSerialization(VueIndexData("""Hello\""", """a""", """""", false, true))
    doTestSerialization(VueIndexData("""Hello\\""", """a""", """""", false, true))
    doTestSerialization(VueIndexData("""Hello\;""", """a""", """""", false, true))
    doTestSerialization(VueIndexData(""";Hello;W""", """a""", """""", false, true))
    doTestSerialization(VueIndexData("""\;Hello;W""", """a""", """""", false, true))
    doTestSerialization(VueIndexData("""Hello;W""", """hello.world""", """hello.world""", false, true))
    doTestSerialization(VueIndexData("""Hello"W"world""", """hello."w".world""", """hello."w".world""", false, true))
  }

  private fun doTestSerialization(data: VueIndexData) {
    val string = serializeUserStringData(data.originalName, data.nameQualifiedReference, data.descriptorQualifiedReference, data.indexedAccessUsed, data.isGlobal)
    val transformed = getVueIndexData(MockImplicitElement(string))
    assertEquals(data, transformed);
  }

  class MockImplicitElement(val data: String) : JSImplicitElementImpl("mock", null) {
    override fun getUserString(): String = "mock"
    override fun getUserStringData(): String = data
  }
}
