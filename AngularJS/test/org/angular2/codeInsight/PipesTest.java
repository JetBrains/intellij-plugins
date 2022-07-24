// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.TypeScriptTestUtil;
import com.intellij.lang.javascript.psi.JSTypeOwner;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.containers.ContainerUtil.sorted;
import static java.util.Arrays.asList;
import static org.angular2.modules.Angular2TestModule.*;
import static org.angularjs.AngularTestUtil.resolveReference;

public class PipesTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "pipes";
  }

  private void doTestAsyncPipeResolution() {
    configureCopy(myFixture, ANGULAR_CORE_8_2_14, ANGULAR_COMMON_8_2_14, RXJS_6_4_0);
    myFixture.configureByFiles("asyncPipe.html", "asyncPipe.ts");

    PsiElement transformMethod = resolveReference("makeObservable() | as<caret>ync", myFixture);
    assertEquals("common.d.ts", transformMethod.getContainingFile().getName());
    assertEquals("transform<T>(obj: Observable<T> | null | undefined): T | null;", transformMethod.getText());

    transformMethod = resolveReference("makePromise() | as<caret>ync", myFixture);
    assertEquals("common.d.ts", transformMethod.getContainingFile().getName());
    assertEquals("transform<T>(obj: Promise<T> | null | undefined): T | null;", transformMethod.getText());

    PsiElement contactField = resolveReference("contact.crea<caret>ted_at", myFixture);
    assertEquals("asyncPipe.ts", contactField.getContainingFile().getName());

    PsiElement contactFieldOptional = resolveReference("(makeObservable() | async)?.leng<caret>th", myFixture);
    assertEquals("lib.es5.d.ts", contactFieldOptional.getContainingFile().getName());
  }

  public void testPipeCompletion() {
    myFixture.configureByFiles("pipe.html", "package.json", "custom.ts");
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertContainsElements(variants, "filta");
  }

  public void testPipeResolve() {
    myFixture.configureByFiles("pipeCustom.resolve.html", "package.json", "custom.ts");
    PsiElement resolve = resolveReference("fil<caret>ta", myFixture);
    assertEquals("custom.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, TypeScriptFunction.class);
    assertInstanceOf(resolve.getParent(), TypeScriptClass.class);
    assertEquals("SearchPipe", ((TypeScriptClass)resolve.getParent()).getName());
  }

  public void testStandardPipesCompletion() {
    configureLink(myFixture, ANGULAR_COMMON_8_2_14);
    myFixture.configureByFiles("pipe.html");
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertContainsElements(variants, "async", "date", "i18nPlural", "i18nSelect", "json", "lowercase",
                           "currency", "number", "percent", "slice", "uppercase", "titlecase", "date");
  }

  public void testNormalPipeResultCompletion() {
    configureLink(myFixture, ANGULAR_COMMON_8_2_14);
    myFixture.configureByFiles("pipeResultCompletion.html");
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertDoesntContain(variants, "wait", "wake", "year", "xml", "stack");
    assertContainsElements(variants, "big", "anchor", "substr");
  }

  public void testGenericClassPipeResultCompletion() {
    configureLink(myFixture, ANGULAR_COMMON_8_2_14);
    myFixture.configureByFiles("genericClassPipe.ts");
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertContainsElements(variants, "bark", "eat");
  }

  public void testAsyncPipeResultCompletion() {
    configureCopy(myFixture, ANGULAR_COMMON_8_2_14, RXJS_6_4_0);
    myFixture.configureByFiles("asyncPipe.html", "asyncPipe.ts");
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertDoesntContain(variants, "wait", "wake", "year", "xml", "stack");
    assertContainsElements(variants, "username", "is_hidden", "email", "created_at", "updated_at");
  }

  public void testAsyncPipeResolutionStrict() {
    TypeScriptTestUtil.setStrictNullChecks(getProject(), getTestRootDisposable());
    doTestAsyncPipeResolution();
  }

  public void testAsyncPipeResolution() {
    TypeScriptTestUtil.forceDefaultTsConfig(getProject(), getTestRootDisposable());
    doTestAsyncPipeResolution();
  }

  public void testAsyncNgIfAsContentAssist() {
    configureCopy(myFixture, ANGULAR_COMMON_8_2_14, RXJS_6_4_0);
    myFixture.configureByFiles("ngIfAs.ts");
    myFixture.completeBasic();
    myFixture.checkResultByFile("ngIfAs.after.ts");
  }

  public void testAsyncNgIfAsObjType() {
    TypeScriptTestUtil.forceConfig(getProject(), null, getTestRootDisposable());
    configureCopy(myFixture, ANGULAR_COMMON_8_2_14, RXJS_6_4_0);
    myFixture.configureByFiles("ngIfAsObj.ts");
    assertEquals("{foo: Person}", ((JSTypeOwner)myFixture.getElementAtCaret()).getJSType().getResolvedTypeText());
  }

  public void testAsyncNgIfAsObjTypeStrictCheck() {
    configureCopy(myFixture, ANGULAR_COMMON_8_2_14, RXJS_6_4_0);
    TypeScriptTestUtil.setStrictNullChecks(getProject(), getTestRootDisposable());
    myFixture.configureByFiles("ngIfAsObj.ts");
    assertEquals("{foo: Person|null}", ((JSTypeOwner)myFixture.getElementAtCaret()).getJSType().getResolvedTypeText());
  }

  public void testContextAware() {
    configureCopy(myFixture, ANGULAR_COMMON_8_2_14, RXJS_6_4_0);
    myFixture.configureByFiles("context-aware.html", "context-aware.ts");
    for (Pair<String, List<String>> check : asList(
      pair("{{ 12 | }}", asList(
        "json#[<any> | json] : <string>#101",
        "i18nPlural#[<number> | i18nPlural:<{[p: string]: string}>:<s…#101")),
      pair("{{ \"test\" | }}", asList(
        "json#[<any> | json] : <string>#101",
        "lowercase#[<string> | lowercase] : <string>#101",
        "titlecase#[<string> | titlecase] : <string>#101",
        "uppercase#[<string> | uppercase] : <string>#101")),
      pair("{{ makePromise() | }}", asList(
        "json#[<any> | json] : <string>#101",
        "async#[<Promise<T>> | async] : <T>#101")),
      pair("{{ makeObservable() | }}", asList(
        "json#[<any> | json] : <string>#101",
        "async#[<Observable<T>> | async] : <T>#101"))
    )) {
      AngularTestUtil.moveToOffsetBySignature(check.first.replace("|", "|<caret>"), myFixture);
      myFixture.completeBasic();
      assertEquals("Issue when checking: " + check.first, sorted(check.second),
                   AngularTestUtil.renderLookupItems(myFixture, true, true)
                     .stream().filter(item -> item.startsWith("json") || item.startsWith("i18nPlural")
                                              || item.startsWith("lowercase") || item.startsWith("titlecase")
                                              || item.startsWith("uppercase") || item.startsWith("async"))
                     .sorted().collect(Collectors.toList()));
    }
  }
}
