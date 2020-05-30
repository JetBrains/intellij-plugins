// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.openapi.application.PathManager
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase

class VueTypeResolveTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/typeResolve/"

  fun testVForJS() {
    myFixture.configureByFile("vFor-js.vue")
    testVFor(Triple("el", "*,number|string", "number"),
             Triple("num", "number", "number"),
             Triple("str", "string", "number"),
             Triple("obj", "#compof(Object),number|string", "string|number"))
  }

  fun testVForTS() {
    myFixture.configureByFile("vFor-ts.vue")
    testVFor(Triple("el", "string", "number"),
             Triple("num", "number", "number"),
             Triple("str", "string", "number"),
             Triple("obj", "boolean", "string"),
             Triple("objNum", "string,string|string", "number"),
             Triple("objMix", "Foo2,string|boolean", "string|number"),
             Triple("objIter", "boolean", "number"),
             Triple("objInit", "number", "\"a\"|\"b\"|\"c\"|\"d\""),
             Triple("state", "ShopState,Foo2", "number"))
  }

  private fun testVFor(vararg testCases: Triple<String, String, String>) {
    for (test in testCases) {
      for (i in 1..3) {
        val element = InjectedLanguageManager.getInstance(project)
          .findInjectedElementAt(myFixture.file, myFixture.file.findOffsetBySignature("{{ ${test.first}<caret>$i"))
          ?.parentOfType<JSReferenceExpression>()
        TestCase.assertNotNull("${test.first}$i", element)
        val type = test.second.split(',').let { if (i == 3) it.last() else it.first() }
        assertEquals("${test.first}$i", type, JSResolveUtil.getElementJSType(element)?.typeText ?: "*")
      }

      val index = InjectedLanguageManager.getInstance(project)
        .findInjectedElementAt(myFixture.file, myFixture.file.findOffsetBySignature("${test.first}<caret>2Ind }}"))
        ?.parentOfType<JSReferenceExpression>()

      TestCase.assertNotNull("${test.first}2Ind", index)
      assertEquals("${test.first}2Ind", test.third, JSResolveUtil.getElementJSType(index)?.typeText ?: "*")
    }
  }
}
