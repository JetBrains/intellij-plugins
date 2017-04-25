package org.angularjs.codeInsight;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.inspections.JSUnresolvedFunctionInspection;
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
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

  public void testInlineTemplateMethodResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("templateMethod.ts", "angular2.js", "customer.ts", "customer2.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ca<caret>ll()", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("customer.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, TypeScriptFunction.class);
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

  public void testNonInlineTemplateUsage2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.enableInspections(JSUnusedLocalSymbolsInspection.class, JSUnusedGlobalSymbolsInspection.class);
      myFixture.configureByFiles("template.usage.ts", "template.usage.html", "angular2.js");
      myFixture.checkHighlighting();
    });
  }

  public void testNonInlineTemplateMethodResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("templateMethod.html", "angular2.js", "templateMethod.ts", "customer.ts", "customer2.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ca<caret>ll()", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("customer.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, TypeScriptFunction.class);
    });
  }

  public void testNonInlineTemplateDefinitionResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("definition.html", "angular2.js", "definition.ts", "definition2.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("tit<caret>le", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("definition.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, TypeScriptFieldImpl.class);
    });
  }

  public void testInlineTemplateDefinitionResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("definition.ts", "angular2.js", "definition2.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("tit<caret>le", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("definition.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, TypeScriptFieldImpl.class);
    });
  }

  public void testNonInlineTemplatePropertyResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("definition2.html", "angular2.js", "definition2.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("check<caret>ed", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("definition2.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, TypeScriptFieldImpl.class);
    });
  }

  public void testInlineTemplatePropertyResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("definition2.ts", "angular2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("check<caret>ed", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("definition2.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, TypeScriptFieldImpl.class);
    });
  }

  public void testInlineTemplateCreateFunction2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.enableInspections(JSUnresolvedFunctionInspection.class);
      myFixture.getAllQuickFixes("createFunction.ts", "angular2.js");
      myFixture.launchAction(myFixture.findSingleIntention("Create Method 'fetchFromApi'"));
      myFixture.checkResultByFile("createFunction.fixed.ts", true);
    });
  }

  public void testInlineTemplateCreateField2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.enableInspections(JSUnresolvedVariableInspection.class);
      myFixture.getAllQuickFixes("createField.ts", "angular2.js");
      myFixture.launchAction(myFixture.findSingleIntention("Create Field 'todo'"));
      myFixture.checkResultByFile("createField.fixed.ts", true);
    });

  }

  public void testNonInlineTemplateCreateFunction2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.enableInspections(JSUnresolvedFunctionInspection.class);
      myFixture.getAllQuickFixes("createFunction.html", "createFunction.ts", "angular2.js");
      myFixture.launchAction(myFixture.findSingleIntention("Create Method 'fetchFromApi'"));
      myFixture.checkResultByFile("createFunction.ts", "createFunction.fixed.ts", true);
    });
  }

  public void testNonInlineTemplateCreateField2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.enableInspections(JSUnresolvedVariableInspection.class);
      myFixture.getAllQuickFixes("createField.html", "createField.ts", "angular2.js");
      myFixture.launchAction(myFixture.findSingleIntention("Create Field 'todo'"));
      myFixture.checkResultByFile("createField.ts", "createField.fixed.ts", true);
    });
  }
}
