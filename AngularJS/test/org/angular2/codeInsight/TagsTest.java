// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.inspections.JSCheckFunctionSignaturesInspection;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.inspections.invalid.CssInvalidPseudoSelectorInspection;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.ThrowableRunnable;
import org.angularjs.AngularTestUtil;

import java.util.List;

public class TagsTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "tags";
  }

  public void testCustomTagsCompletion20TypeScript() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> myFixture.testCompletion("custom.html", "custom.after.html", "package.json", "custom.ts"));
  }

  public void testCustomTagsCompletion20NoNormalize() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> myFixture.testCompletion("custom_no_normalize.html", "custom_no_normalize.after.html", "package.json", "custom_no_normalize.ts"));
  }

  public void testCustomTagsCompletion20JavaScript() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> myFixture.testCompletion("custom.html", "custom.after.html", "package.json", "custom2.js"));
  }

  public void testCustomTagsResolve20TypeScriptComponent() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.configureByFiles("custom.after.html", "package.json", "custom.ts");
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
      myFixture.configureByFiles("custom.after.html", "package.json", "custom_directive.ts");
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
      myFixture.configureByFiles("custom.after.html", "package.json", "custom2.js");
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

  public void testDeepPseudoSelector() {
    myFixture.enableInspections(CssInvalidPseudoSelectorInspection.class);
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> myFixture.testHighlighting("deepPseudoSelector.html", "package.json"));
  }

  public void testCustomSpaceSeparated() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> {
                                          final List<String> variants = myFixture.getCompletionVariants("customSpaceSeparated.html", "customSpaceSeparated.ts", "package.json");
                                          assertContainsElements(variants, "dummy-list", "dummy-nav-list");
                                        });
  }

  public void testInlineTemplateHtmlTags() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      List<String> variants = myFixture.getCompletionVariants("inline_template.ts", "package.json");
      assertContainsElements(variants, "a", "img", "my-customer");
    });
  }

  public void testNgContainerCompletion20() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        () -> myFixture.testCompletion("ngContainer.html", "ngContainer.after.html", "package.json"));
  }


  public void testNgContainerResolve20() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.configureByFiles("ngContainer.after.html", "package.json");
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
                                        () -> myFixture.testCompletion("ngTemplate.html", "ngTemplate.after.html", "package.json"));
  }


  public void testNgTemplateResolve20() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.configureByFiles("ngTemplate.after.html", "package.json");
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
                                        () -> myFixture.testCompletion("ngContent.html", "ngContent.after.html", "package.json"));
  }


  public void testNgContentResolve20() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.configureByFiles("ngContent.after.html", "package.json");
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
      myFixture.configureByFiles("ionic.html", "package.json", "ionic.metadata.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "ion-item");
    });
  }

  public void testNoNormalizedResolve20() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.configureByFiles("noNormalized.ts", "package.json");
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

  public void testTagClassTypes() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.enableInspections(JSCheckFunctionSignaturesInspection.class);
      myFixture.configureByFiles("tagClassTypes.ts", "package.json");
      myFixture.checkHighlighting(true, false, true);
    });
  }


}
