// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.inspections.invalid.CssInvalidPseudoSelectorInspection;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.inspections.AngularUndefinedBindingInspection;
import org.angularjs.AngularTestUtil;

import java.util.List;

public class TagsTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "tags";
  }

  public void testCustomTagsCompletion20TypeScript() {
    myFixture.testCompletion("custom.html", "custom.after.html", "package.json", "custom.ts");
  }

  public void testCustomTagsCompletion20NoNormalize() {
    myFixture.testCompletion(
      "custom_no_normalize.html", "custom_no_normalize.after.html", "package.json",
      "custom_no_normalize.ts");
  }

  public void testCustomTagsResolve20TypeScriptComponent() {
    myFixture.configureByFiles("custom.after.html", "package.json", "custom.ts");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.ts", resolve.getContainingFile().getName());
    assertEquals("@Component({\n" +
                 "    selector: 'my-customer',\n" +
                 "    properties: {\n" +
                 "        'id':'dependency'\n" +
                 "    }\n" +
                 "})", AngularTestUtil.getDirectiveDefinitionText(resolve));
  }

  public void testCustomTagsResolve20TypeScriptDirective() {
    myFixture.configureByFiles("custom.after.html", "package.json", "custom_directive.ts");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom_directive.ts", resolve.getContainingFile().getName());
    assertEquals("@Directive({\n" +
                 "    selector: 'my-customer',\n" +
                 "    properties: {\n" +
                 "        'id':'dependency'\n" +
                 "    }\n" +
                 "})", AngularTestUtil.getDirectiveDefinitionText(resolve));
  }

  public void testDeepPseudoSelector() {
    myFixture.enableInspections(CssInvalidPseudoSelectorInspection.class);
    myFixture.testHighlighting("deepPseudoSelector.html", "package.json");
  }

  public void testCustomSpaceSeparated() {
    final List<String> variants = myFixture.getCompletionVariants(
      "customSpaceSeparated.html", "customSpaceSeparated.ts", "package.json");
    assertContainsElements(variants, "dummy-list", "dummy-nav-list");
  }

  public void testInlineTemplateHtmlTags() {
    List<String> variants = myFixture.getCompletionVariants("inline_template.ts", "package.json");
    assertContainsElements(variants, "a", "img", "my-customer");
  }

  public void testNgContainerCompletion20() {
    myFixture.testCompletion("ngContainer.html", "ngContainer.after.html", "package.json");
  }


  public void testNgContainerResolve20() {
    myFixture.configureByFiles("ngContainer.after.html", "package.json");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-<caret>container", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("ngContainer.after.html", resolve.getContainingFile().getName());
    assertEquals("<ng-container", AngularTestUtil.getDirectiveDefinitionText(resolve));
  }

  public void testNgTemplateCompletion20() {
    myFixture.testCompletion("ngTemplate.html", "ngTemplate.after.html", "package.json");
  }


  public void testNgTemplateResolve20() {
    myFixture.configureByFiles("ngTemplate.after.html", "package.json");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-<caret>template", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("ngTemplate.after.html", resolve.getContainingFile().getName());
    assertEquals("<ng-template", AngularTestUtil.getDirectiveDefinitionText(resolve));
  }

  public void testNgContentCompletion20() {
    myFixture.testCompletion("ngContent.html", "ngContent.after.html", "package.json");
  }


  public void testNgContentResolve20() {
    myFixture.configureByFiles("ngContent.after.html", "package.json");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-<caret>content", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("ngContent.after.html", resolve.getContainingFile().getName());
    assertEquals("<ng-content", AngularTestUtil.getDirectiveDefinitionText(resolve));
  }

  public void testNoNormalizedResolve20() {
    myFixture.configureByFiles("noNormalized.ts", "package.json");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("app_<caret>hello", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNull(resolve);
  }

  public void testTagClassTypes() {
    myFixture.enableInspections(TypeScriptValidateTypesInspection.class);
    myFixture.configureByFiles("tagClassTypes.ts", "package.json");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testHtmlWithDoctype() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class,
                                HtmlUnknownTagInspection.class);
    myFixture.configureByFiles("withDoctype.html", "package.json");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testComponentNavigation() {
    myFixture.copyDirectoryToProject("component-navigation", ".");
    for (int i = 1; i <= 4; i++) {
      myFixture.configureFromTempProjectFile("app" + i + "/app/app.component.html");
      String filePath = AngularTestUtil.resolveReference("<app-<caret>test>", myFixture).getContainingFile()
        .getVirtualFile()
        .getPath();
      assertTrue(filePath + " should have " + i, filePath.endsWith("/app" + i + "/app/test.component.ts"));
    }
  }
}
