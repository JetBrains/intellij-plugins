// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JSTestUtils.runInBackgroundReadActionAndWait
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSAliasTypeImpl
import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.findOffsetBySignature
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.parentOfTypes
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

private const val PREFIX_INTERPOLATION = "{{ "

@TestApplication
class VueTypeResolveTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/typeResolve/")
  private val myFixture get() = hybrid.fixture
  private val project get() = myFixture.project
  private val testRootDisposable get() = myFixture.testRootDisposable

  @Test
  fun testVForJS(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile("vFor-js.vue")
    testVFor(Triple("el", "any,number | string", "number"),
             Triple("num", "number", "number"),
             Triple("str", "string", "number"),
             Triple("obj", "any,string | number", "string | number"))
  }

  @Test
  fun testVForTS(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
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

  @Test
  fun testVForScriptSetupTS(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByFile("vForScriptSetup-ts.vue")
    testVFor(
      Triple("unionItem", "string | number", "number"),
      Triple("cast", "string | number | boolean", "\"name\" | \"age\" | \"verified\""),
      Triple("aliased", "string | boolean", "\"name\" | \"verified\""),
      iterations = 2
    )
  }

  @Test
  fun testVForScriptSetupTS_strict(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFile("vForScriptSetup-ts-strict.vue")
    testVFor(
      Triple("item", "string", "number"),
      Triple("itemByRef", "string", "number"),
      iterations = 2
    )
  }

  @Test
  fun testVForScriptSetupJS(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByFile("vForScriptSetup-js.vue")
    testVFor(
      Triple("empty", "never", "never"),
      Triple("person", "string | number | boolean", "\"name\" | \"age\" | \"verified\""),
      iterations = 2
    )
  }

  @Test
  fun testPropsWithDefaultTS(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
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

  @Test
  fun testPropsWithDefaultScriptSetupTS(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFile("propsWithDefaultScriptSetup-ts.vue")

    doTest(
      "msg" to "string | undefined",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testModelPropsTS(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFile("modelProps-ts.vue")

    doTest(
      "modelValue" to "string | undefined",
      "numDefault" to "123 | 234 | undefined",
      "nameRequired" to "string",
      "nameRequiredGeneric" to "string",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testScriptCaseSensitivity(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFile("scriptCaseSensitivity.vue")

    doTest(
      "example" to "RealType",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testSlotTS(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile("slot-ts.vue")

    doTest(
      "row" to "{uid: boolean}",
      "row.u<caret>id" to "boolean",
      "bag" to "{row: {uid: boolean}}",
      "bag.row.u<caret>id" to "boolean",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testInject(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

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

  @Test
  fun testInjectStrictNullChecks(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

    doTest(
      "message" to "\"hello\" | undefined",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testInjectWithDefault(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

    doTest(
      "message" to "\"hello\"",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testInjectSymbol(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

    doTest(
      "myLocalInject" to "{name: string}",
      "myLocalInject.n<caret>ame" to "string",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testInjectSymbolTs(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

    doTest(
      "myLocalInject" to "{name: string}",
      "myLocalInject.n<caret>ame" to "\"bob\"",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testInjectSymbolProvideInCall(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

    doTest(
      "myLocalInject" to "{name: string}",
      "myLocalInject.n<caret>ame" to "string",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testInjectSymbolProvideInApp(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

    doTest(
      "myLocalInject" to "{name?: string, lang?: Lang}",
      "myLocalInject.n<caret>ame" to "string",
      "myLocalInject.la<caret>ng" to "Lang",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testInjectCallTypeEvaluation(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

    doTest(
      "stringInject" to "string",
      "keyInject" to "boolean",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testInjectCallTypeEvaluationVue2(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_7_14)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

    doTest(
      "stringInject" to "string",
      "keyInject" to "boolean",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testPropsWithJsDoc(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

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

  @Test
  fun testPropsVue2WithJsDoc(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_7_14)
    myFixture.configureByFile("${hybrid.getTestName(true)}.vue")

    doTest(
      "barProp" to "{userName: string, password: string}",
      "barProp.user<caret>Name" to "string",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testTypeofImportRef(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    // WEB-56524
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByFile("typeof-import-ref.ts")
    assertEquals("Ref&lt;UnwrapRef&lt;string&gt;&gt;", JSTestUtils.getExpressionTypeFromEditor(myFixture))
  }

  @Test
  fun testDefineSlotType(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

    doTest(
      "pageTitle" to "string | undefined",
      "msg" to "string",
      "footerProps" to "{year?: number}",
      "footerProps.ye<caret>ar" to "number | undefined",
      "pageTitleFromComponent" to "string | undefined",
      prefix = PREFIX_INTERPOLATION,
    )
  }

  @Test
  fun testDefineSlotDefault(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

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

  @Test
  fun testDefineExpose(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

    doTest(
      "type HelloPro<caret>ps" to "{readonly msg: string}",
      "type HelloProps<caret>PropRef" to "string",
      "type ShouldBeUn<caret>Refed" to "number",
      "const resu<caret>lt1" to "number | undefined",
      "const resu<caret>lt2" to "boolean | undefined",
    )
  }

  @Test
  fun testDefineSlotTypedProp(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), "")
    myFixture.configureFromTempProjectFile("${hybrid.getTestName(false)}.vue")

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
        assertNotNull(element, "${test.first}$i")
        val type = test.second.split(',').let { if (i == 3) it.last() else it.first() }
        assertEquals(type, getElementTypeText(element), "${test.first}$i")
      }

      val index = findElementBySignature("${test.first}<caret>2Ind }}")
      assertNotNull(index, "${test.first}2Ind")
      assertEquals(test.third, getElementTypeText(index), "${test.first}2Ind")
    }
  }

  private fun doTest(vararg testCases: Pair<String, String>, prefix: String = "") {
    runInBackgroundReadActionAndWait {
      for (test in testCases) {
        val caretMarker = if (test.first.contains("<caret>")) "" else "<caret>"
        val element = findElementBySignature("$prefix$caretMarker${test.first}")
        assertNotNull(element, test.first)
        val expected = test.second
        assertEquals(expected, getElementTypeText(element), test.first)
      }
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
