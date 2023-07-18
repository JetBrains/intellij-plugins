// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSTestUtils
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
      Triple("cast", "string | number | boolean", "\"name\" | \"age\" | \"verified\""),
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
      Triple("person", "string | number | boolean", "\"name\" | \"age\" | \"verified\""),
      iterations = 2
    )
  }

  fun testPropsWithDefaultTS() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFile("propsWithDefault-ts.vue")

    val strictUnion = "\"center\" | \"left\" | \"right\""
    val optionalUnion = "\"center\" | \"left\" | \"right\" | undefined"

    doTest(
      "align1" to strictUnion,
      "align2" to optionalUnion,
      "align3" to strictUnion,
      "align4" to strictUnion,
      "align5" to optionalUnion,
      "align6" to optionalUnion,
      "align7" to optionalUnion,
      "align8" to strictUnion,
      "bool" to "boolean",
      "boolOptional" to "boolean | undefined",
      "boolDefault" to "boolean",
      "boolNotRequired" to "boolean",
    )
  }

  fun testPropsWithDefaultScriptSetupTS() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFile("propsWithDefaultScriptSetup-ts.vue")

    doTest("msg" to "string")
  }

  fun testModelPropsTS() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_2)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFile("modelProps-ts.vue")

    doTest(
      "modelValue" to "string | undefined",
      "numDefault" to "123 | 234",
      "nameRequired" to "string",
      "nameRequiredGeneric" to "string",
    )
  }

  fun testScriptCaseSensitivity() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFile("scriptCaseSensitivity.vue")

    doTest(
      "example" to "RealType",
    )
  }

  fun testSlotTS() {
    myFixture.configureByFile("slot-ts.vue")

    doTest(
      "row" to "{uid: boolean}",
      "row.u<caret>id" to "boolean",
      "bag" to "{row: {uid: boolean}}",
      "bag.row.u<caret>id" to "boolean",
    )
  }

  fun testInject() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_2)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "literal" to "string",
      "func" to "boolean",
      "computed" to "number",
      "funcData" to "string",
      "app" to "number",
      "scriptSetup" to "boolean",
      "scriptSetupRef" to "string",
      "globalProvide" to "number",
      "globalProvideRef" to "number",
    )
  }

  fun testInjectStrictNullChecks() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_2)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "message" to "\"hello\" | undefined",
    )
  }

  fun testInjectWithDefault() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_2)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "message" to "\"hello\"",
    )
  }

  fun testInjectSymbol() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_2)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "myLocalInject" to "{name: string}",
      "myLocalInject.n<caret>ame" to "string",
    )
  }

  fun testInjectSymbolTs() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_2)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "myLocalInject" to "{name: string}",
      "myLocalInject.n<caret>ame" to "\"bob\"",
    )
  }

  fun testInjectSymbolProvideInCall() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_2)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "myLocalInject" to "{name: string}",
      "myLocalInject.n<caret>ame" to "string",
    )
  }

  fun testInjectSymbolProvideInApp() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_2)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "myLocalInject" to "{name?: string, lang?: Lang}",
      "myLocalInject.n<caret>ame" to "string",
      "myLocalInject.la<caret>ng" to "Lang",
    )
  }

  fun testInjectCallTypeEvaluation() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_2)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "stringInject" to "string",
      "keyInject" to "boolean",
    )
  }

  fun testInjectCallTypeEvaluationVue2() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_7_14)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "stringInject" to "string",
      "keyInject" to "boolean",
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
      val caretMarker = if (test.first.contains("<caret>")) "" else "<caret>"
      val element = findReferenceBySignature("$prefix$caretMarker${test.first}")
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

  fun testTypeofImportRef() {
    // WEB-56524
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByFile("typeof-import-ref.ts")
    assertEquals("Ref&lt;UnwrapRef&lt;string&gt;&gt;", JSTestUtils.getExpressionTypeFromEditor(myFixture))
  }
}
