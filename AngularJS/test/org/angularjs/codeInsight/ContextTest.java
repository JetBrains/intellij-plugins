package org.angularjs.codeInsight;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptFieldImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.ThrowableRunnable;
import org.angularjs.AngularTestUtil;

/**
 * @author Dennis.Ushakov
 */
public class ContextTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "context";
  }

  public void testInlineTemplateCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        (ThrowableRunnable<Exception>)() -> myFixture.testCompletion("component.ts", "component.after.ts", "angular2.js"));
  }

  public void testInlineTemplateResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("component.after.ts", "angular2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("=\"onComple<caret>tedButton()", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("component.after.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSFunction.class);
    });
  }

  public void testNonInlineTemplateCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        (ThrowableRunnable<Exception>)() -> myFixture.testCompletion("template.completion.html", "template.html", "angular2.js", "template.completion.ts"));
  }

  public void testNonInlineTemplateResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("template.html", "angular2.js", "template.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("myCu<caret>", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("template.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, TypeScriptFieldImpl.class);
    });
  }
}
