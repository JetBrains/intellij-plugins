// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angularjs.AngularTestUtil;

public class InjectionsTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "injections";
  }

  public void testAngular2EmptyInterpolation() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() ->
      myFixture.testCompletion("emptyInterpolation.html", "emptyInterpolation.after.html", "package.json", "emptyInterpolation.ts")
    );
  }

  public void testAngular2NonEmptyInterpolation() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() ->
      myFixture.testCompletion("nonEmptyInterpolation.html", "nonEmptyInterpolation.after.html", "package.json", "nonEmptyInterpolation.ts")
    );
  }

  public void testEventHandler2Resolve() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("event.html", "package.json", "event.ts");
      checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
    });
  }

  public void testEventHandlerPrivate2Resolve() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("event_private.html", "package.json", "event_private.ts");
      checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
    });
  }

  public void testNgIfResolve() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("ngIf.ts", "ng_if.ts", "package.json");
      checkVariableResolve("my_use<caret>r.last", "my_user", JSVariable.class);
    });
  }

  private <T extends JSElement> T checkVariableResolve(final String signature, final String varName, final Class<T> varClass) {
    return AngularTestUtil.checkVariableResolve(signature, varName, varClass, myFixture);
  }

  public void testStyles2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.ts", "package.json");
      final int offset = AngularTestUtil.findOffsetBySignature("Helvetica <caret>Neue", myFixture.getFile());
      final PsiElement element = InjectedLanguageManager.getInstance(getProject()).findInjectedElementAt(myFixture.getFile(), offset);
      assertEquals(CSSLanguage.INSTANCE, element.getLanguage());
    });
  }

  public void testHost() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("host.ts", "package.json");
      for (Pair<String, ? extends JSLanguageDialect> signature : ContainerUtil.newArrayList(
        Pair.create("eve<caret>nt", Angular2Language.INSTANCE),
        Pair.create("bind<caret>ing", Angular2Language.INSTANCE),
        Pair.create("at<caret>tribute", JavaScriptSupportLoader.TYPESCRIPT))) {
        final int offset = AngularTestUtil.findOffsetBySignature(signature.first, myFixture.getFile());
        PsiElement element = InjectedLanguageManager.getInstance(getProject()).findInjectedElementAt(myFixture.getFile(), offset);
        if (element == null) {
          element = myFixture.getFile().findElementAt(offset);
        }
        assertEquals(signature.first, signature.second, element.getContainingFile().getLanguage());
      }
    });
  }

  public void testNgForExternalCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(),
                                        () -> myFixture.testCompletion("ngFor.html", "ngFor.after.html", "package.json"));
  }

  public void testNgForExternalResolve() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("ngFor.after.html", "ngFor.ts", "ng_for_of.ts", "package.json");
      JSVariable resolve = checkVariableResolve("\"myTo<caret>do\"", "myTodo", JSVariable.class);
      //TODO support pipe type evaluation
      //assertEquals("string", resolve.getTypeString());
    });
  }

  public void testNgForInlineCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(),
                                        () -> myFixture.testCompletion("ngFor.ts", "ngFor.after.ts", "package.json"));
  }

  public void testNgForInlineResolve() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("ngFor.after.ts", "ng_for_of.ts", "package.json");
      checkVariableResolve("\"myTo<caret>do\"", "myTodo", JSVariable.class);
    });
  }

  public void test$EventExternalCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(),
                                        () -> myFixture.testCompletion("$event.html", "$event.after.html", "package.json"));
  }

  public void test$EventInlineCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(),
                                        () -> myFixture.testCompletion("$event.ts", "$event.after.ts", "package.json"));
  }

  public void testUserSpecifiedInjection() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("userSpecifiedLang.ts", "package.json");
      for (Pair<String, String> signature : ContainerUtil.newArrayList(
        Pair.create("<div><caret></div>", Angular2HtmlLanguage.INSTANCE.getID()),
        Pair.create("$text<caret>-color", "SCSS"), //fails if correct order of injectors is not ensured
        Pair.create("color: <caret>#00aa00", CSSLanguage.INSTANCE.getID()))) {

        final int offset = AngularTestUtil.findOffsetBySignature(signature.first, myFixture.getFile());
        final PsiElement element = InjectedLanguageManager.getInstance(getProject()).findInjectedElementAt(myFixture.getFile(), offset);
        assertEquals(signature.first, signature.second, element.getContainingFile().getLanguage().getID());
      }
    });
  }

  public void testNoInjectionInHTMLTemplateLiteral() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("noInjection.html", "package.json");
      int offset = AngularTestUtil.findOffsetBySignature("b<caret>ar", myFixture.getFile());
      assert offset > 0;
      PsiElement injection = InjectedLanguageManager.getInstance(getProject()).findInjectedElementAt(myFixture.getFile(), offset);
      assertNull("There should be no injection", injection);
    });
  }

  public void testPrivateMembersOrder() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("event_private.html", "event_private.ts", "package.json");
      myFixture.completeBasic();
      assertEquals("Private members should be sorted after public ones", myFixture.getLookupElementStrings(),
                   ContainerUtil.newArrayList("callSecuredApi", "callZ", "_callApi", "callA", "callAnonymousApi"));
    });
  }
}
