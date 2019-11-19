// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.JSTypeOwner;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import java.util.List;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.containers.ContainerUtil.sorted;
import static java.util.Arrays.asList;
import static org.angularjs.AngularTestUtil.configureWithMetadataFiles;
import static org.angularjs.AngularTestUtil.resolveReference;

public class PipesTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "pipes";
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
    configureWithMetadataFiles(myFixture, "common");
    myFixture.configureByFiles("pipe.html");
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertContainsElements(variants, "async", "date", "i18nPlural", "i18nSelect", "json", "lowercase",
                           "currency", "number", "percent", "slice", "uppercase", "titlecase", "date");
  }

  public void testNormalPipeResultCompletion() {
    configureWithMetadataFiles(myFixture, "common");
    myFixture.configureByFiles("pipeResultCompletion.html", "json_pipe.d.ts");
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertDoesntContain(variants, "wait", "wake", "year", "xml", "stack");
    assertContainsElements(variants, "big", "anchor", "substr");
  }

  public void testAsyncPipeResultCompletion() {
    configureWithMetadataFiles(myFixture, "common");
    myFixture.configureByFiles("asyncPipe.html", "asyncPipe.ts", "async_pipe.d.ts", "Observable.d.ts");
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertDoesntContain(variants, "wait", "wake", "year", "xml", "stack");
    assertContainsElements(variants, "username", "is_hidden", "email", "created_at", "updated_at");
  }

  public void testAsyncPipeResolution() {
    configureWithMetadataFiles(myFixture, "common");
    myFixture.configureByFiles("asyncPipe.html", "asyncPipe.ts", "async_pipe.d.ts", "Observable.d.ts", "ng_for_of.d.ts");

    PsiElement transformMethod = resolveReference("makeObservable() | as<caret>ync", myFixture);
    assertEquals("async_pipe.d.ts", transformMethod.getContainingFile().getName());
    assertEquals("transform<T>(obj: Observable<T> | null | undefined): T | null;", transformMethod.getText());

    transformMethod = resolveReference("makePromise() | as<caret>ync", myFixture);
    assertEquals("async_pipe.d.ts", transformMethod.getContainingFile().getName());
    assertEquals("transform<T>(obj: Promise<T> | null | undefined): T | null;", transformMethod.getText());

    PsiElement contactField = resolveReference("contact.crea<caret>ted_at", myFixture);
    assertEquals("asyncPipe.ts", contactField.getContainingFile().getName());
  }

  public void testAsyncNgIfAsContentAssist() {
    configureWithMetadataFiles(myFixture, "common");
    myFixture.configureByFiles("ngIfAs.ts", "async_pipe.d.ts", "Observable.d.ts");
    myFixture.completeBasic();
    myFixture.checkResultByFile("ngIfAs.after.ts");
  }

  public void testAsyncNgIfAsObjType() {
    configureWithMetadataFiles(myFixture, "common");
    myFixture.configureByFiles("ngIfAsObj.ts", "async_pipe.d.ts", "Observable.d.ts");
    assertEquals("{foo: Person}", ((JSTypeOwner)myFixture.getElementAtCaret()).getJSType().getResolvedTypeText());
  }

  public void testContextAware() {
    myFixture.configureByFiles("context-aware.html", "context-aware.ts", "json_pipe.ts", "async_pipe.ts",
                               "i18n_plural_pipe.ts", "case_conversion_pipes.ts", "Observable.d.ts", "package.json");
    for (Pair<String, List<String>> check : asList(
      pair("{{ 12 | }}", asList(
        "json#[<any> | json] : <string>#100",
        "i18nPlural#[<number> | i18nPlural:<{[p: string]: string}>:<s…#101")),
      pair("{{ \"test\" | }}", asList(
        "json#[<any> | json] : <string>#100",
        "lowercase#[<string> | lowercase] : <string>#101",
        "titlecase#[<string> | titlecase] : <string>#100",
        "uppercase#[<string> | uppercase] : <string>#100")),
      pair("{{ makePromise() | }}", asList(
        "json#[<any> | json] : <string>#100",
        "async#[<Promise<T>> | async] : <T>#101")),
      pair("{{ makeObservable() | }}", asList(
        "json#[<any> | json] : <string>#100",
        "async#[<Observable<T>> | async] : <T>#101"))
    )) {
      AngularTestUtil.moveToOffsetBySignature(check.first.replace("|", "|<caret>"), myFixture);
      myFixture.completeBasic();
      assertEquals("Issue when checking: " + check.first, sorted(check.second),
                   sorted(AngularTestUtil.renderLookupItems(myFixture, true, true)));
    }
  }
}
