package org.angularjs.codeInsight;

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection;
import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.angularjs.AngularTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestApplication
public class AttributesTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "attributes");
  }

  private static String getDirectiveDefinitionText(PsiElement resolve) {
    return resolve.getParent().getText();
  }

  @Test
  public void testStandardAttributesCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("standard.html", "standard.after.html", "angular.js");
  }

  @Test
  public void testNgInclude() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("ng-include.html", "ng-include.after.html", "angular.js");
  }

  @Test
  public void testStandardAttributesResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("standard.after.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-mo<caret>del", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("angular.js", resolve.getContainingFile().getName());
    });
  }

  @Test
  public void testStandardAttributesResolveOldStyle() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("standard.after.html", "angular12.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-mo<caret>del", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("angular12.js", resolve.getContainingFile().getName());
    });
  }

  @Test
  public void testStandardAttributesDataResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("standard-data.after.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-mo<caret>del", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("angular.js", resolve.getContainingFile().getName());
    });
  }

  @Test
  public void testCustomAttributesInDirectiveCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("customInDirective.html", "customInDirective.after.html", "custom.js", "angular.js");
  }

  @Test
  public void testCustomAttributesInDirectiveResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("customInDirective.after.html", "custom.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.js", resolve.getContainingFile().getName());
      assertEquals("'myCustomer'", getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testCustomAttributesInDirectiveEmptyCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("customInDirectiveEmpty.html", "customInDirectiveEmpty.after.html", "custom.js", "angular.js");
  }

  @Test
  public void testCustomAttributesInDirectiveEmptyResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("customInDirectiveEmpty.after.html", "custom.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.js", resolve.getContainingFile().getName());
      assertEquals("'myCustomer'", getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testCustomAttributesCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("custom.html", "custom.after.html", "custom.js", "angular.js");
  }

  @Test
  public void testCustomAttributesNoCompletionIfNoAngular() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("custom.html", "custom.no.completion.after.html", "custom.js");
  }

  @Test
  public void testCustomAttributesTemplateCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("custom.html", "custom.after.html", "custom_template.js", "angular.js");
  }

  @Test
  public void testCustomAttributesCompletion15() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("custom.html", "custom.after.html", "custom15.js", "angular.js");
  }

  @Test
  public void testCustomAttributesResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("custom.after.html", "custom.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.js", resolve.getContainingFile().getName());
      assertEquals("'myCustomer'", getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testCustomAttributesTemplateResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("custom.after.html", "custom_template.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom_template.js", resolve.getContainingFile().getName());
      assertEquals("`myCustomer`", getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testCustomAttributesResolve15() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("custom.after.html", "custom15.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom15.js", resolve.getContainingFile().getName());
      assertEquals("'myCustomer'", getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testControllerCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletionTyping("controller.html", "\n", "controller.after.html", "custom.js", "angular.js");
  }

  @Test
  public void testControllerResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("controller.resolve.html", "custom.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("Supa<caret>Controller", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.js", resolve.getContainingFile().getName());
      assertEquals("'SupaController'", getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testPrefixedControllerResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("controller.prefixed.html", "custom.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("Supa<caret>Controller", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.js", resolve.getContainingFile().getName());
      assertEquals("'SupaController'", getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testAppCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("app.html", "app.after.html", "custom.js", "angular.js");
  }

  @Test
  public void testAppResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("app.after.html", "custom.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("docs<caret>SimpleDirective", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.js", resolve.getContainingFile().getName());
      assertEquals("'docsSimpleDirective'", getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testNormalization() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("normalize.html", "angular.js");
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    myFixture.enableInspections(XmlUnboundNsPrefixInspection.class);
    myFixture.checkHighlighting();
  }

  @Test
  public void testNgSrc() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ng-src.html", "angular.js");
    myFixture.enableInspections(RequiredAttributesInspection.class);
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    myFixture.checkHighlighting();
  }

  @Test
  public void testNgSrcCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ng-src.completion.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("img ng-<caret>", myFixture.getFile());
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature));
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).contains("ng-src");

    int divOffsetBySignature = AngularTestUtil.findOffsetBySignature("div ng-<caret>", myFixture.getFile());
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getEditor().getCaretModel().moveToOffset(divOffsetBySignature));
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).doesNotContain("ng-src");
  }

  @Test
  public void testRestrictE() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("form.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("div f<caret>", myFixture.getFile());
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature));
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).doesNotContain("form");
  }

  @Test
  public void testIncorrectJSDoc() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    //noinspection NonAsciiCharacters
    myFixture.configureByText(System.currentTimeMillis() + ".js",
                              """
                                /**
                                 * @ngdoc directive
                                 * @name yaSelect
                                 * @restrict E
                                 *
                                 * @param description
                                 *
                                 * @description Р’С‹РІРѕРґРёС‚ select
                                 *
                                 * @param ngModel Assignable angular expression to data-bind to. sa
                                 * bla bla bla l
                                 */""");
    myFixture.doHighlighting();
  }

  @Test
  public void testInlineStyle() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("style.html", "angular.js");
    myFixture.checkHighlighting();
  }

  @Test
  public void testElement() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ng-copy.html", "angular.js");
    for (String signature : new String[]{"input", "select", "textarea", "a"}) {
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("<" + signature + " ng-<caret>", myFixture.getFile());
      EdtTestUtil.runInEdtAndWait(() -> myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature));
      myFixture.completeBasic();
      assertThat(myFixture.getLookupElementStrings()).contains("ng-copy");
    }
  }

  @Test
  public void testInputElement() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ng-disabled.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("<button ng-<caret>", myFixture.getFile());
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature));
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).contains("ng-disabled");
  }

  @Test
  public void testInputWithParent() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ng-disabled-parent.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("<button ng-<caret>", myFixture.getFile());
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature));
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).contains("ng-disabled");
  }

  @Test
  public void testInputWithNgForm() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ng-disabled-ng-form.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("<button ng-<caret>", myFixture.getFile());
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature));
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).contains("ng-disabled");
  }

  @Test
  public void testRepeatCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ng-repeat.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("<div ng-rep<caret>", myFixture.getFile());
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature));
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).contains("ng-repeat", "ng-repeat-start", "ng-repeat-end");
  }

  @Test
  public void testRepeatResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ng-repeat.resolve.html", "angular.js");
    ReadAction.run(() -> {
      for (String suffix : new String[]{"", "-start", "-end"}) {
        int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng<caret>-repeat" + suffix, myFixture.getFile());
        PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
        assertNotNull(ref);
        PsiElement resolve = ref.resolve();
        assertNotNull(resolve);
        assertEquals("angular.js", resolve.getContainingFile().getName());
      }
    });
  }

  @Test
  public void testSrcInjection() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("srcInjection.html", "angular.js");
    myFixture.enableInspections(HtmlUnknownTargetInspection.class);
    myFixture.checkHighlighting();
  }

  @Test
  public void testComponent15AttributesCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("component15.html", "component15.after.html", "angular.js", "component15.js");
  }

  @Test
  public void testComponent15AttributesResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("component15.after.html", "angular.js", "component15.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("he<caret>ro=", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("component15.js", resolve.getContainingFile().getName());
      assertEquals("hero: '<'", getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testNgOptionsHighlighting() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ng-options.html", "angular.js");
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    myFixture.checkHighlighting();
  }

}
