package org.angularjs.codeInsight;

import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.ThrowableRunnable;
import com.intellij.xml.util.CheckValidXmlInScriptBodyInspection;
import org.angularjs.AngularTestUtil;

import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class TagsTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "tags";
  }

  @Override
  protected boolean isWriteActionRequired() {
    return getTestName(true).contains("Completion");
  }

  public void testStandardTagsCompletion() {
    myFixture.testCompletion("standard.html", "standard.after.html", "angular.js");
  }

  public void testStandardAttributesCompletion() {
    myFixture.testCompletion("standardAttributes.html", "standardAttributes.after.html", "angular.js");
  }

  public void testStandardTagsResolve() {
    myFixture.configureByFiles("standard.after.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-fo<caret>rm", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("angular.js", resolve.getContainingFile().getName());
  }

  public void testCustomTagsCompletion() {
    myFixture.testCompletion("custom.html", "custom.after.html", "angular.js", "custom.js");
  }

  public void testCustomTagsCompletion13() {
    myFixture.testCompletion("custom13.html", "custom13.after.html", "angular13.js", "custom.js");
  }

  public void testCustomTagsCompletion20TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), new ThrowableRunnable<Exception>() {
      @Override
      public void run() throws Exception {
        myFixture.testCompletion("custom.html", "custom.after.html", "angular2.js", "custom.ts");
      }
    });
  }

  public void testCustomTagsCompletion20JavaScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), new ThrowableRunnable<Exception>() {
      @Override
      public void run() throws Exception {
        myFixture.testCompletion("custom.html", "custom.after.html", "angular2.js", "custom2.js");
      }
    });
  }

  public void testCustomTagsViaFunctionCompletion() {
    myFixture.testCompletion("customViaFunction.html", "customViaFunction.after.html", "angular.js", "custom.js");
  }

  public void testCustomTagsArrayViaFunctionCompletion() {
    myFixture.testCompletion("customArrayViaFunction.html", "customArrayViaFunction.after.html", "angular.js", "custom.js");
  }

  public void testCustomTagsViaFunctionForwardCompletion() {
    myFixture.testCompletion("customViaFunctionForward.html", "customViaFunctionForward.after.html", "angular.js", "custom.js");
  }

  public void testCustomTagsArrayCompletion() {
    myFixture.testCompletion("customArray.html", "customArray.after.html", "angular.js", "custom.js");
  }

  public void testCustomAttributesCompletion() {
    myFixture.testCompletion("customAttributes.html", "customAttributes.after.html", "angular.js", "custom.js");
  }

  public void testCommonAttributesCompletion() {
    myFixture.testCompletion("commonAttributes.html", "commonAttributes.after.html", "angular.js", "custom.js");
  }

  public void testCustomTagsResolve() {
    myFixture.configureByFiles("custom.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'myCustomer'", getDirectiveDefinitionText(resolve));
  }

  public void testCustomTagsResolve20TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), new ThrowableRunnable<Exception>() {
      @Override
      public void run() throws Exception {
        myFixture.configureByFiles("custom.after.html", "angular2.js", "custom.ts");
        int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
        PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
        assertNotNull(ref);
        PsiElement resolve = ref.resolve();
        assertNotNull(resolve);
        assertEquals("custom.ts", resolve.getContainingFile().getName());
        assertEquals("Component({\n" +
                     "    selector: '[my-customer]',\n" +
                     "    properties: {\n" +
                     "        'id':'dependency'\n" +
                     "    }\n" +
                     "})", getDirectiveDefinitionText(resolve));
      }
    });
  }

  public void testCustomTagsResolve20JavaScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), new ThrowableRunnable<Exception>() {
      @Override
      public void run() throws Exception {
        myFixture.configureByFiles("custom.after.html", "angular2.js", "custom2.js");
        int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
        PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
        assertNotNull(ref);
        PsiElement resolve = ref.resolve();
        assertNotNull(resolve);
        assertEquals("custom2.js", resolve.getContainingFile().getName());
        assertEquals("new angular.ComponentAnnotation({\n" +
                     "    selector: 'my-customer'\n" +
                     "  })", getDirectiveDefinitionText(resolve));
      }
    });
  }

  private static String getDirectiveDefinitionText(PsiElement resolve) {
    return resolve.getParent().getText();
  }

  public void testCustomTagsViaFunctionResolve() {
    myFixture.configureByFiles("customViaFunction.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("function-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'functionCustomer'", getDirectiveDefinitionText(resolve));
  }

  public void testCustomTagsViaFunctionForwardResolve() {
    myFixture.configureByFiles("customViaFunctionForward.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("great-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'greatCustomer'", getDirectiveDefinitionText(resolve));
  }


  public void testCustomTagsArrayViaFunctionResolve() {
    myFixture.configureByFiles("customArrayViaFunction.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("array-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'arrayCustomer'", getDirectiveDefinitionText(resolve));
  }

  public void testCustomTagsArrayResolve() {
    myFixture.configureByFiles("customArray.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("her-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'herCustomer'", getDirectiveDefinitionText(resolve));
  }

  public void testOverride() {
    myFixture.enableInspections(CheckValidXmlInScriptBodyInspection.class);
    myFixture.configureByFiles("override.html", "angular.js");
    myFixture.checkHighlighting();
  }

  public void testProperNamespace() {
    myFixture.enableInspections(RequiredAttributesInspection.class);
    myFixture.configureByFiles("namespace.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }

  public void testCustomTagsCompletionCss() {
    myFixture.testCompletion("customCss.html", "customCss.after.html", "angular.js", "custom.js");
  }

  public void testCustomTagsResolveCss() {
    myFixture.configureByFiles("customCss.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'myCustomer'", getDirectiveDefinitionText(resolve));
  }

  public void testNoCompletionInXml() {
    final List<String> variants = myFixture.getCompletionVariants("standard.xml", "angular.js");
    assertDoesntContain(variants, "ng-form", "form", "script");
  }

  public void testUnclosed() {
    myFixture.configureByFiles("unclosed.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }
}
