// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.webSymbols.findOffsetBySignature
import junit.framework.TestCase

class VueTypeResolveTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getVueTestDataPath() + "/typeResolve/"

  fun testVForJS() {
    myFixture.configureByFile("vFor-js.vue")
    testVFor(Triple("el", "any,number | string", "number"),
             Triple("num", "number", "number"),
             Triple("str", "string", "number"),
             Triple("obj", "any,number | string", "string | number"))
  }

  fun testVForTS() {
    myFixture.configureByFile("vFor-ts.vue")
    testVFor(Triple("el", "string", "number"),
             Triple("num", "number", "number"),
             Triple("str", "string", "number"),
             Triple("obj", "boolean", "string"),
             Triple("objNum", "string,string", "number"),
             Triple("objMix", "Foo2,string | boolean", "number | string"),
             Triple("objIter", "boolean", "number"),
             Triple("objInit", "number", "\"a\" | \"b\" | \"c\" | \"d\""),
             Triple("state", "ShopState,Foo2", "number"),
             Triple("union", "number | string", "number"))
  }

  fun testVForScriptSetupTS() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByFile("vForScriptSetup-ts.vue")
    testVFor(
      Triple("unionItem", "string | number", "number"),
      Triple("cast", "string | boolean | number", "\"name\" | \"age\" | \"verified\""),
      Triple("aliased", "string | boolean", "\"name\" | \"verified\""),
      iterations = 2
    )
  }

  fun testVForScriptSetupTS_strict() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFile("vForScriptSetup-ts-strict.vue")
    testVFor(
      Triple("item", "string", "number"),
      Triple("itemByRef", "string", "number"),
      iterations = 2
    )
  }

  fun testVForScriptSetupJS() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByFile("vForScriptSetup-js.vue")
    testVFor(
      Triple("empty", "never", "never"),
      Triple("person", "string | boolean | number", "\"name\" | \"age\" | \"verified\""),
      iterations = 2
    )
  }

  fun testPropsWithDefaultTS() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFile("propsWithDefault-ts.vue")

    val strict = "\"center\" | \"left\" | \"right\""
    val optional = "\"center\" | \"left\" | \"right\" | undefined"

    doTest(
      "align1" to strict,
      "align2" to optional,
      "align3" to strict,
      "align4" to strict,
      "align5" to optional,
      "align6" to optional,
      "align7" to optional,
      "align8" to strict,
    )
  }

  private fun testVFor(vararg testCases: Triple<String, String, String>, iterations: Int = 3) {
    for (test in testCases) {
      for (i in 1..iterations) {
        val element = findReferenceBySignature("{{ ${test.first}<caret>$i")
        TestCase.assertNotNull("${test.first}$i", element)
        val type = test.second.split(',').let { if (i == 3) it.last() else it.first() }
        assertEquals("${test.first}$i", type, getElementTypeText(element))
      }

      val index = findReferenceBySignature("${test.first}<caret>2Ind }}")
      TestCase.assertNotNull("${test.first}2Ind", index)
      assertEquals("${test.first}2Ind", test.third, getElementTypeText(index))
    }
  }

  private fun doTest(vararg testCases: Pair<String, String>, prefix: String = "{{ ") {
    for (test in testCases) {
      val element = findReferenceBySignature("$prefix<caret>${test.first}")
      TestCase.assertNotNull(test.first, element)
      val expected = test.second
      assertEquals(test.first, expected, getElementTypeText(element))
    }
  }

  private fun getElementTypeText(element: JSReferenceExpression?) =
    JSResolveUtil.getElementJSType(element)?.getTypeText(JSType.TypeTextFormat.PRESENTABLE) ?: "any"

  private fun findReferenceBySignature(signature: String) = InjectedLanguageManager.getInstance(project)
    .findInjectedElementAt(myFixture.file, myFixture.file.findOffsetBySignature(signature))
    ?.parentOfType<JSReferenceExpression>()
}
