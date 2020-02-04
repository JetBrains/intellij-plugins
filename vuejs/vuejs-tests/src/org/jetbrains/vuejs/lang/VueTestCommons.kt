// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.testFramework.UsefulTestCase
import junit.framework.TestCase
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.toAsset

class VueTestCommons : UsefulTestCase() {

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
      TestCase.assertEquals(test.first, test.second, fromAsset(test.first))
    }
  }

  fun testToAsset() {
    for (test in listOf(
      "foo" to "foo",
      "foo-bar" to "fooBar",
      "foo-bar-foo" to "fooBarFoo",
      "url20-converter" to "url20Converter",
      "url20converter" to "url20converter",
      "foo-b-a-r-foo" to "fooBARFoo"
    )) {
      TestCase.assertEquals(test.first, test.second, toAsset(test.first))
    }
  }

}
