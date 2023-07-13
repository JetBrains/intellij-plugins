// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.deprecated

import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.psi.JSTypeOwner
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.containers.ContainerUtil
import com.intellij.webSymbols.moveToOffsetBySignature
import com.intellij.webSymbols.resolveReference
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureCopy
import org.angular2.Angular2TestModule.Companion.configureLink
import org.angularjs.AngularTestUtil

@Deprecated("Use test appropriate for IDE feature being tested - e.g. completion/resolve/highlighting ")
class Angular2PipesTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "deprecated/pipes"
  }

  private fun doTestAsyncPipeResolution() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14,
                  Angular2TestModule.RXJS_6_4_0)
    myFixture.configureByFiles("asyncPipe.html", "asyncPipe.ts")
    var transformMethod = myFixture.resolveReference("makeObservable() | as<caret>ync")
    assertEquals("common.d.ts", transformMethod.getContainingFile().getName())
    assertEquals("transform<T>(obj: Observable<T> | null | undefined): T | null;", transformMethod.getText())
    transformMethod = myFixture.resolveReference("makePromise() | as<caret>ync")
    assertEquals("common.d.ts", transformMethod.getContainingFile().getName())
    assertEquals("transform<T>(obj: Promise<T> | null | undefined): T | null;", transformMethod.getText())
    val contactField = myFixture.resolveReference("contact.crea<caret>ted_at")
    assertEquals("asyncPipe.ts", contactField.getContainingFile().getName())
    val contactFieldOptional = myFixture.resolveReference("(makeObservable() | async)?.leng<caret>th")
    assertEquals("lib.es5.d.ts", contactFieldOptional.getContainingFile().getName())
  }

  fun testPipeCompletion() {
    myFixture.configureByFiles("pipe.html", "package.json", "custom.ts")
    myFixture.completeBasic()
    val variants = myFixture.getLookupElementStrings()!!
    UsefulTestCase.assertContainsElements(variants, "filta")
  }

  fun testPipeResolve() {
    myFixture.configureByFiles("pipeCustom.resolve.html", "package.json", "custom.ts")
    val resolve = myFixture.resolveReference("fil<caret>ta")
    assertEquals("custom.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, TypeScriptFunction::class.java)
    UsefulTestCase.assertInstanceOf(resolve.getParent(), TypeScriptClass::class.java)
    assertEquals("SearchPipe", (resolve.getParent() as TypeScriptClass).getName())
  }

  fun testStandardPipesCompletion() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_8_2_14)
    myFixture.configureByFiles("pipe.html")
    myFixture.completeBasic()
    val variants = myFixture.getLookupElementStrings()!!
    UsefulTestCase.assertContainsElements(variants, "async", "date", "i18nPlural", "i18nSelect", "json", "lowercase",
                                          "currency", "number", "percent", "slice", "uppercase", "titlecase", "date")
  }

  fun testNormalPipeResultCompletion() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_8_2_14)
    myFixture.configureByFiles("pipeResultCompletion.html")
    myFixture.completeBasic()
    val variants = myFixture.getLookupElementStrings()!!
    UsefulTestCase.assertDoesntContain(variants, "wait", "wake", "year", "xml", "stack")
    UsefulTestCase.assertContainsElements(variants, "big", "anchor", "substr")
  }

  fun testGenericClassPipeResultCompletion() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_8_2_14)
    myFixture.configureByFiles("genericClassPipe.ts")
    myFixture.completeBasic()
    val variants = myFixture.getLookupElementStrings()!!
    UsefulTestCase.assertContainsElements(variants, "bark", "eat")
  }

  fun testAsyncPipeResultCompletion() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_COMMON_8_2_14, Angular2TestModule.RXJS_6_4_0)
    myFixture.configureByFiles("asyncPipe.html", "asyncPipe.ts")
    myFixture.completeBasic()
    val variants = myFixture.getLookupElementStrings()!!
    UsefulTestCase.assertDoesntContain(variants, "wait", "wake", "year", "xml", "stack")
    UsefulTestCase.assertContainsElements(variants, "username", "is_hidden", "email", "created_at", "updated_at")
  }

  fun testAsyncPipeResolutionStrict() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    doTestAsyncPipeResolution()
  }

  fun testAsyncPipeResolution() {
    TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    doTestAsyncPipeResolution()
  }

  fun testAsyncNgIfAsContentAssist() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_COMMON_8_2_14, Angular2TestModule.RXJS_6_4_0)
    myFixture.configureByFiles("ngIfAs.ts")
    myFixture.completeBasic()
    myFixture.checkResultByFile("ngIfAs.after.ts")
  }

  fun testAsyncNgIfAsObjType() {
    TypeScriptTestUtil.forceConfig(project, null, testRootDisposable)
    configureCopy(myFixture, Angular2TestModule.ANGULAR_COMMON_8_2_14, Angular2TestModule.RXJS_6_4_0)
    myFixture.configureByFiles("ngIfAsObj.ts")
    assertEquals("{foo: Person}", (myFixture.getElementAtCaret() as JSTypeOwner).getJSType()!!.resolvedTypeText)
  }

  fun testAsyncNgIfAsObjTypeStrictCheck() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_COMMON_8_2_14, Angular2TestModule.RXJS_6_4_0)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFiles("ngIfAsObj.ts")
    assertEquals("{foo: Person|null}", (myFixture.getElementAtCaret() as JSTypeOwner).getJSType()!!.resolvedTypeText)
  }

  fun testMixinPipes() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_16_0_0_NEXT_4, Angular2TestModule.ANGULAR_COMMON_16_0_0_NEXT_4)
    myFixture.configureByFiles("mixinPipes.ts")
    myFixture.checkHighlighting()
  }

  fun testContextAware() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_COMMON_8_2_14, Angular2TestModule.RXJS_6_4_0)
    myFixture.configureByFiles("context-aware.html", "context-aware.ts")
    for (check in listOf(
      Pair("{{ 12 | }}", mutableListOf(
        "json#[<any> | json] : <string>#101",
        "i18nPlural#[<number> | i18nPlural:<{[p: string]: string}>:<s…#101")),
      Pair("{{ \"test\" | }}", mutableListOf(
        "json#[<any> | json] : <string>#101",
        "lowercase#[<string> | lowercase] : <string>#101",
        "titlecase#[<string> | titlecase] : <string>#101",
        "uppercase#[<string> | uppercase] : <string>#101")),
      Pair("{{ makePromise() | }}", mutableListOf(
        "json#[<any> | json] : <string>#101",
        "async#[<Promise<T>> | async] : <T>#101")),
      Pair("{{ makeObservable() | }}", mutableListOf(
        "json#[<any> | json] : <string>#101",
        "async#[<Observable<T>> | async] : <T>#101"))
    )) {
      myFixture.moveToOffsetBySignature(check.first.replace("|", "|<caret>"))
      myFixture.completeBasic()
      assertEquals("Issue when checking: " + check.first, ContainerUtil.sorted(check.second),
                   AngularTestUtil.renderLookupItems(myFixture, true, true)
                     .filter { item: String ->
                       (item.startsWith("json") || item.startsWith("i18nPlural")
                        || item.startsWith("lowercase") || item.startsWith("titlecase")
                        || item.startsWith("uppercase") || item.startsWith("async"))
                     }
                     .sorted())
    }
  }

  fun testTypedPipe() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_COMMON_16_0_0_NEXT_4, Angular2TestModule.ANGULAR_CORE_16_0_0_NEXT_4)
    myFixture.configureByFile("typedPipe.ts")
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.checkHighlighting()
  }
}
