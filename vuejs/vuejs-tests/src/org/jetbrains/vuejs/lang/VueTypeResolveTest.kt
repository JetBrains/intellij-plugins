// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSAliasTypeImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.parentOfTypes
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.webSymbols.testFramework.findOffsetBySignature
import junit.framework.TestCase

private const val PREFIX_INTERPOLATION = "{{ "

class VueTypeResolveTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getVueTestDataPath() + "/typeResolve/"

  fun testVForJS() {
    myFixture.configureByFile("vFor-js.vue")
    testVFor(Triple("el", "any,number | string", "number"),
             Triple("num", "number", "number"),
             Triple("str", "string", "number"),
             Triple("obj", "any,string | number", "string | number"))
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
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testPropsWithDefaultScriptSetupTS() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFile("propsWithDefaultScriptSetup-ts.vue")

    doTest(
      "msg" to "string",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testModelPropsTS() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFile("modelProps-ts.vue")

    doTest(
      "modelValue" to "string | undefined",
      "numDefault" to "123 | 234",
      "nameRequired" to "string",
      "nameRequiredGeneric" to "string",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testScriptCaseSensitivity() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFile("scriptCaseSensitivity.vue")

    doTest(
      "example" to "RealType",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testSlotTS() {
    myFixture.configureByFile("slot-ts.vue")

    doTest(
      "row" to "{uid: boolean}",
      "row.u<caret>id" to "boolean",
      "bag" to "{row: {uid: boolean}}",
      "bag.row.u<caret>id" to "boolean",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testInject() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
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
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testInjectStrictNullChecks() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "message" to "\"hello\" | undefined",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testInjectWithDefault() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "message" to "\"hello\"",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testInjectSymbol() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "myLocalInject" to "{name: string}",
      "myLocalInject.n<caret>ame" to "string",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testInjectSymbolTs() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "myLocalInject" to "{name: string}",
      "myLocalInject.n<caret>ame" to "\"bob\"",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testInjectSymbolProvideInCall() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "myLocalInject" to "{name: string}",
      "myLocalInject.n<caret>ame" to "string",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testInjectSymbolProvideInApp() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "myLocalInject" to "{name?: string, lang?: Lang}",
      "myLocalInject.n<caret>ame" to "string",
      "myLocalInject.la<caret>ng" to "Lang",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testInjectCallTypeEvaluation() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "stringInject" to "string",
      "keyInject" to "boolean",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testInjectCallTypeEvaluationVue2() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_7_14)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "stringInject" to "string",
      "keyInject" to "boolean",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testPropsWithJsDoc() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "prop1" to "{strVar: (string | string[])}",
      "prop1.str<caret>Var" to "string | string[]",
      "prop2" to "Prop2",
      "prop2.strV<caret>ar2" to "string | string[]",
      "prop1Obj" to "{strVar: (string | string[])}",
      "prop1Obj.str<caret>Var" to "string | string[]",
      "prop2Obj" to "Prop2",
      "prop2Obj.strVa<caret>r2" to "string | string[]",
      "prop1Val" to "string | string[]",
      "prop2Val" to "string | string[]",
      "barProp" to "{userName: string, password: string}",
      "barProp.user<caret>Name" to "string",
      "barProp1" to "{userName: string, password: string}",
      "barProp1.passwo<caret>rd" to "string",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testPropsVue2WithJsDoc() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_7_14)
    myFixture.configureByFile("${getTestName(true)}.vue")

    doTest(
      "barProp" to "{userName: string, password: string}",
      "barProp.user<caret>Name" to "string",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testTypeofImportRef() {
    // WEB-56524
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByFile("typeof-import-ref.ts")
    assertEquals("Ref&lt;UnwrapRef&lt;string&gt;&gt;", JSTestUtils.getExpressionTypeFromEditor(myFixture))
  }

  fun testDefineSlotType() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "pageTitle" to "string | undefined",
      "msg" to "string",
      "footerProps" to "{year?: number}",
      "footerProps.ye<caret>ar" to "number | undefined",
      "pageTitleFromComponent" to "string | undefined",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testDefineSlotDefault() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "fie<caret>ld.class" to "FieldSlotPropText",
      "field.cla<caret>ss" to "string | string[]",
      "fi<caret>eld.style" to "FieldSlotPropText",
      "field.sty<caret>le" to "string | object",
      "fie<caret>ld.value" to "FieldSlotPropText",
      "field.valu<caret>e" to "object | (() => object)",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  fun testDefineExpose() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "type HelloPro<caret>ps" to "{readonly msg: string}",
      "type HelloProps<caret>PropRef" to "string",
      "type ShouldBeUn<caret>Refed" to "number",
      "const resu<caret>lt1" to "number | undefined",
      "const resu<caret>lt2" to "boolean | undefined",
    )
  }

  fun testDefineSlotTypedProp() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")

    doTest(
      "bar" to "number",
      "company" to "string",
      "data" to "{year?: number}",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  private fun testVFor(vararg testCases: Triple<String, String, String>, iterations: Int = 3) {
    for (test in testCases) {
      for (i in 1..iterations) {
        val element = findElementBySignature("{{ ${test.first}<caret>$i")
        TestCase.assertNotNull("${test.first}$i", element)
        val type = test.second.split(',').let { if (i == 3) it.last() else it.first() }
        assertEquals("${test.first}$i", type, getElementTypeText(element))
      }

      val index = findElementBySignature("${test.first}<caret>2Ind }}")
      TestCase.assertNotNull("${test.first}2Ind", index)
      assertEquals("${test.first}2Ind", test.third, getElementTypeText(index))
    }
  }

  private fun doTest(vararg testCases: Pair<String, String>, prefix: String = "") {
    for (test in testCases) {
      val caretMarker = if (test.first.contains("<caret>")) "" else "<caret>"
      val element = findElementBySignature("$prefix$caretMarker${test.first}")
      TestCase.assertNotNull(test.first, element)
      val expected = test.second
      assertEquals(test.first, expected, getElementTypeText(element))
    }
  }

  private fun getElementTypeText(element: PsiElement?) =
    JSResolveUtil.getElementJSType(element)?.substitute()
      ?.let { if (it is JSAliasTypeImpl) it.originalType.substitute() else it }
      ?.getTypeText(JSType.TypeTextFormat.PRESENTABLE) ?: "any"

  private fun findElementBySignature(signature: String): PsiElement? {
    val offset = myFixture.file.findOffsetBySignature(signature)
    val element =
      InjectedLanguageManager.getInstance(project).findInjectedElementAt(myFixture.file, offset) ?: myFixture.file.findElementAt(offset)
    return element?.parentOfTypes(JSReferenceExpression::class, PsiNamedElement::class)
  }
}
