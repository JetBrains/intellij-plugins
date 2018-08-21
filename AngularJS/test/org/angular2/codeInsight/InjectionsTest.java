// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.inspections.UnterminatedStatementJSInspection;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angularjs.AngularTestUtil;
import org.angularjs.lang.AngularJSLanguage;

public class InjectionsTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "injections";
  }

  public void testAngular2EmptyInterpolation() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() ->
      myFixture.testCompletion("emptyInterpolation.html", "emptyInterpolation.after.html", "angular2.js", "emptyInterpolation.ts")
    );
  }

  public void testAngular2NonEmptyInterpolation() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() ->
      myFixture.testCompletion("nonEmptyInterpolation.html", "nonEmptyInterpolation.after.html", "angular2.js", "nonEmptyInterpolation.ts")
    );
  }

  public void testEventHandler2Resolve() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("event.html", "angular2.js", "event.ts");
      checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
    });
  }

  public void testBinding2Resolve() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.html", "angular2.js", "event.ts");
      checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
    });
  }

  public void testBindingPrivate2Resolve() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.html", "angular2.js", "event_private.ts");
      checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
    });
  }

  public void testNgIfResolve() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("ngIf.ts", "angular2.js", "ngIf.ts");
      checkVariableResolve("my_use<caret>r.last", "my_user", JSDefinitionExpression.class);
    });
  }

  private PsiElement checkVariableResolve(final String signature, final String varName, final Class<? extends JSElement> varClass) {
    int offsetBySignature = AngularTestUtil.findOffsetBySignature(signature, myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertInstanceOf(resolve, varClass);
    assertEquals(varName, varClass.cast(resolve).getName());
    return resolve;
  }

  public void testStyles2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.ts", "angular2.js");
      final int offset = AngularTestUtil.findOffsetBySignature("Helvetica <caret>Neue", myFixture.getFile());
      final PsiElement element = InjectedLanguageUtil.findElementAtNoCommit(myFixture.getFile(), offset);
      assertEquals(CSSLanguage.INSTANCE, element.getLanguage());
    });
  }

  public void testHost() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("host.ts", "angular2.js");
      for (Pair<String, ? extends JSLanguageDialect> signature : ContainerUtil.newArrayList(
        Pair.create("eve<caret>nt", AngularJSLanguage.INSTANCE),
        Pair.create("bind<caret>ing", AngularJSLanguage.INSTANCE),
        Pair.create("at<caret>tribute", JavaScriptSupportLoader.TYPESCRIPT))) {
        final int offset = AngularTestUtil.findOffsetBySignature(signature.first, myFixture.getFile());
        final PsiElement element = InjectedLanguageUtil.findElementAtNoCommit(myFixture.getFile(), offset);
        assertEquals(signature.first, signature.second, element.getContainingFile().getLanguage());
      }
    });
  }

  public void testUnterminated() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.enableInspections(UnterminatedStatementJSInspection.class);
      myFixture.configureByFiles("unterminated.ts", "angular2.js");
      myFixture.checkHighlighting();
    });
  }

  public void testNgForExternalCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(),
                                        () -> myFixture.testCompletion("ngFor.html", "ngFor.after.html", "angular2.js"));
  }

  public void testNgForExternalResolve() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("ngFor.after.html", "angular2.js");
      checkVariableResolve("myTo<caret>do", "myTodo", JSVariable.class);
    });
  }

  public void testNgForInlineCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(),
                                        () -> myFixture.testCompletion("ngFor.ts", "ngFor.after.ts", "angular2.js"));
  }

  public void testNgForInlineResolve() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("ngFor.after.ts", "angular2.js");
      checkVariableResolve("\"myTo<caret>do\"", "myTodo", JSVariable.class);
    });
  }

  public void test$EventExternalCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(),
                                        () -> myFixture.testCompletion("$event.html", "$event.after.html", "angular2.js"));
  }

  public void test$EventInlineCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(),
                                        () -> myFixture.testCompletion("$event.ts", "$event.after.ts", "angular2.js"));
  }

  public void testUserSpecifiedInjection() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("userSpecifiedLang.ts", "angular2.js");
      for (Pair<String, String> signature : ContainerUtil.newArrayList(
        Pair.create("<div><caret></div>", Angular2HtmlLanguage.INSTANCE.getID()),
        Pair.create("$text<caret>-color", "SCSS"), //fails if correct order of injectors is not ensured
        Pair.create("color: <caret>#00aa00", CSSLanguage.INSTANCE.getID()))) {

        final int offset = AngularTestUtil.findOffsetBySignature(signature.first, myFixture.getFile());
        final PsiElement element = InjectedLanguageUtil.findElementAtNoCommit(myFixture.getFile(), offset);
        assertEquals(signature.first, signature.second, element.getContainingFile().getLanguage().getID());
      }
    });
  }

}
