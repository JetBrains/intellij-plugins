// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor

import com.intellij.javascript.JSParameterInfoHandler
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.parameterInfo.CreateParameterInfoContext
import com.intellij.lang.typescript.hint.TypeScriptParameterInfoHandler
import com.intellij.psi.PsiElement
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.utils.parameterInfo.MockCreateParameterInfoContext
import com.intellij.testFramework.utils.parameterInfo.MockParameterInfoUIContext
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angularjs.AngularTestUtil

class Angular2PipeParameterInfoTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass)
  }

  fun testPipeParameterInfo() {
    doTest("value: number, exponent: string")
  }

  private fun doTest(expected: String) {
    myFixture.configureByFiles(getTestName(false) + ".ts", "package.json")
    val parameterInfoContext: CreateParameterInfoContext = MockCreateParameterInfoContext(myFixture.getEditor(), myFixture.getFile())
    val list = TypeScriptParameterInfoHandler().findElementForParameterInfo(parameterInfoContext)
    assertNotNull(list)
    val items = parameterInfoContext.getItemsToShow()!!
    val strings = items.map { getPresentation(it) }
    UsefulTestCase.assertSize(1, strings)
    assertEquals(expected, strings[0])
  }

  companion object {
    private fun getPresentation(parameterInfoElement: Any): String {
      assertTrue(parameterInfoElement is JSFunctionType)
      val jsFunctionType = parameterInfoElement as JSFunctionType
      val parameterInfoHandler = JSParameterInfoHandler()
      val context = MockParameterInfoUIContext<PsiElement?>(jsFunctionType.getSourceFunctionItem())
      parameterInfoHandler.updateUI(jsFunctionType, context)
      return context.text
    }
  }
}
