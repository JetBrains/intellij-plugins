// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import java.util.List;

import static org.angularjs.AngularTestUtil.resolveReference;

public class PipesTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "pipes";
  }

  public void testPipeCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("pipe.html", "package.json", "custom.ts");
      myFixture.completeBasic();
      final List<String> variants = myFixture.getLookupElementStrings();
      assertContainsElements(variants, "filta");
    });
  }

  public void testPipeResolve() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("pipeCustom.resolve.html", "package.json", "custom.ts");
      PsiElement resolve = resolveReference("fil<caret>ta", myFixture);
      assertEquals("custom.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, TypeScriptFunction.class);
      assertInstanceOf(resolve.getParent(), TypeScriptClass.class);
      assertEquals("SearchPipe", ((TypeScriptClass)resolve.getParent()).getName());
    });
  }

  public void testStandardPipesCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("pipe.html", "package.json", "common.metadata.json");
      myFixture.completeBasic();
      final List<String> variants = myFixture.getLookupElementStrings();
      assertContainsElements(variants, "async", "date", "i18nPlural", "i18nSelect", "json", "lowercase",
                             "currency", "number", "percent", "slice", "uppercase", "titlecase", "date");
    });
  }

  public void testNormalPipeResultCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("pipeResultCompletion.html", "package.json", "common.metadata.json", "json_pipe.d.ts");
      myFixture.completeBasic();
      final List<String> variants = myFixture.getLookupElementStrings();
      assertDoesntContain(variants, "wait", "wake", "year", "xml", "stack");
      assertContainsElements(variants, "big", "anchor", "substr");
    });
  }

  public void testAsyncPipeResultCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("asyncPipe.html", "asyncPipe.ts", "package.json", "common.metadata.json", "async_pipe.d.ts", "Observable.d.ts");
      myFixture.completeBasic();
      final List<String> variants = myFixture.getLookupElementStrings();
      assertDoesntContain(variants, "wait", "wake", "year", "xml", "stack");
      assertContainsElements(variants, "username", "is_hidden", "email", "created_at", "updated_at");
    });
  }

  public void testAsyncPipeResolution() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("asyncPipe.html", "asyncPipe.ts", "package.json", "common.metadata.json", "async_pipe.d.ts", "Observable.d.ts", "ng_for_of.d.ts");

      PsiElement transformMethod = resolveReference("makeObservable() | as<caret>ync", myFixture);
      assertEquals("async_pipe.d.ts", transformMethod.getContainingFile().getName());
      assertEquals("transform<T>(obj: Observable<T> | null | undefined): T | null;", transformMethod.getText());

      transformMethod = resolveReference("makePromise() | as<caret>ync", myFixture);
      assertEquals("async_pipe.d.ts", transformMethod.getContainingFile().getName());
      assertEquals("transform<T>(obj: Promise<T> | null | undefined): T | null;", transformMethod.getText());

      PsiElement contactField = resolveReference("contact.crea<caret>ted_at", myFixture);
      assertEquals("asyncPipe.ts", contactField.getContainingFile().getName());
    });
  }

}
