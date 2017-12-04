package org.angularjs.codeInsight;

import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.inspections.invalid.CssInvalidPseudoSelectorInspection;
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

  public void testCustomTagsCompletion15() {
    myFixture.testCompletion("custom13.html", "custom13.after.html", "angular13.js", "custom15.js");
  }

  public void testCustomTagsCompletion20TypeScript() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> myFixture.testCompletion("custom.html", "custom.after.html", "angular2.js", "custom.ts"));
  }

  public void testCustomTagsCompletion20NoNormalize() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> myFixture.testCompletion("custom_no_normalize.html", "custom_no_normalize.after.html", "angular2.js", "custom_no_normalize.ts"));
  }

  public void testCustomTagsCompletion20JavaScript() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> myFixture.testCompletion("custom.html", "custom.after.html", "angular2.js", "custom2.js"));
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
    assertEquals("'myCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
  }

  public void testCustomTagsResolve15() {
    myFixture.configureByFiles("custom13.after.html", "angular.js", "custom15.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("new-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom15.js", resolve.getContainingFile().getName());
    assertEquals("'newCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
  }

  public void testCustomTagsResolve20TypeScriptComponent() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.configureByFiles("custom.after.html", "angular2.js", "custom.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.ts", resolve.getContainingFile().getName());
      assertEquals("Component({\n" +
                   "    selector: 'my-customer',\n" +
                   "    properties: {\n" +
                   "        'id':'dependency'\n" +
                   "    }\n" +
                   "})", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  public void testCustomTagsResolve20TypeScriptDirective() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.configureByFiles("custom.after.html", "angular2.js", "custom_directive.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom_directive.ts", resolve.getContainingFile().getName());
      assertEquals("Directive({\n" +
                   "    selector: 'my-customer',\n" +
                   "    properties: {\n" +
                   "        'id':'dependency'\n" +
                   "    }\n" +
                   "})", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  public void testCustomTagsResolve20JavaScript() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.configureByFiles("custom.after.html", "angular2.js", "custom2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom2.js", resolve.getContainingFile().getName());
      assertEquals("new angular.ComponentAnnotation({\n" +
                   "    selector: 'my-customer'\n" +
                   "  })", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  public void testCustomTagsViaFunctionResolve() {
    myFixture.configureByFiles("customViaFunction.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("function-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'functionCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
  }

  public void testCustomTagsViaFunctionForwardResolve() {
    myFixture.configureByFiles("customViaFunctionForward.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("great-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'greatCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
  }


  public void testCustomTagsArrayViaFunctionResolve() {
    myFixture.configureByFiles("customArrayViaFunction.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("array-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'arrayCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
  }

  public void testCustomTagsArrayResolve() {
    myFixture.configureByFiles("customArray.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("her-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'herCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
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

  public void testDeepPseudoSelector() {
    myFixture.enableInspections(CssInvalidPseudoSelectorInspection.class);
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> myFixture.testHighlighting("deepPseudoSelector.html", "angular2.js"));
  }

  public void testCustomTagsResolveCss() {
    myFixture.configureByFiles("customCss.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'myCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
  }

  public void testNoCompletionInXml() {
    final List<String> variants = myFixture.getCompletionVariants("standard.xml", "angular.js");
    assertDoesntContain(variants, "ng-form", "form", "script");
  }

  public void testCustomSpaceSeparated() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> {
                                          final List<String> variants = myFixture.getCompletionVariants("customSpaceSeparated.html", "customSpaceSeparated.ts", "angular2.js");
                                          assertContainsElements(variants, "dummy-list", "dummy-nav-list");
                                        });
  }

  public void testUnclosed() {
    myFixture.configureByFiles("unclosed.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }

  public void testInlineTemplateHtmlTags() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      List<String> variants = myFixture.getCompletionVariants("inline_template.ts", "angular2.js");
      assertContainsElements(variants, "a", "img", "my-customer");
    });
  }

  public void testNgContainerCompletion20() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> myFixture.testCompletion("ngContainer.html", "ngContainer.after.html", "angular2.js"));
  }


  public void testNgContainerResolve20() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.configureByFiles("ngContainer.after.html", "angular2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-<caret>container", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("ngContainer.after.html", resolve.getContainingFile().getName());
      assertEquals("<ng-container></ng-container>", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  public void testNgTemplateCompletion20() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> myFixture.testCompletion("ngTemplate.html", "ngTemplate.after.html", "angular2.js"));
  }


  public void testNgTemplateResolve20() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.configureByFiles("ngTemplate.after.html", "angular2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-<caret>template", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("ngTemplate.after.html", resolve.getContainingFile().getName());
      assertEquals("<ng-template></ng-template>", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  public void testNgContentCompletion20() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> myFixture.testCompletion("ngContent.html", "ngContent.after.html", "angular2.js"));
  }


  public void testNgContentResolve20() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.configureByFiles("ngContent.after.html", "angular2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-<caret>content", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("ngContent.after.html", resolve.getContainingFile().getName());
      assertEquals("<ng-content></ng-content>", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  public void testComplexSelectorList2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("ionic.html", "angular2.js", "ionic.metadata.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "ion-item");
    });
  }

  public void testNoNormalizedResolve20() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.configureByFiles("noNormalized.ts", "angular2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("app_<caret>hello", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("noNormalized.ts", resolve.getContainingFile().getName());
      assertEquals("Component({\n" +
                   "    selector: 'app_hello',\n" +
                   "    template: '<app_hello></app_hello>',\n" +
                   "})", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

}
