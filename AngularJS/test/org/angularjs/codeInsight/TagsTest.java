package org.angularjs.codeInsight;

import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.xml.util.CheckValidXmlInScriptBodyInspection;
import org.angularjs.AngularTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dennis.Ushakov
 */
@TestApplication
public class TagsTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "tags");
  }

  @Test
  public void testStandardTagsCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("standard.html", "standard.after.html", "angular.js");
  }

  @Test
  public void testStandardAttributesCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("standardAttributes.html", "standardAttributes.after.html", "angular.js");
  }

  @Test
  public void testStandardTagsResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("standard.after.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-fo<caret>rm", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("angular.js", resolve.getContainingFile().getName());
    });
  }

  @Test
  public void testCustomTagsCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("custom.html", "custom.after.html", "angular.js", "custom.js");
  }

  @Test
  public void testCustomTagsCompletion13() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("custom13.html", "custom13.after.html", "angular13.js", "custom.js");
  }

  @Test
  public void testCustomTagsCompletion15() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("custom13.html", "custom13.after.html", "angular13.js", "custom15.js");
  }

  @Test
  public void testCustomTagsViaFunctionCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("customViaFunction.html", "customViaFunction.after.html", "angular.js", "custom.js");
  }

  @Test
  public void testCustomTagsArrayViaFunctionCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("customArrayViaFunction.html", "customArrayViaFunction.after.html", "angular.js", "custom.js");
  }

  @Test
  public void testCustomTagsViaFunctionForwardCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("customViaFunctionForward.html", "customViaFunctionForward.after.html", "angular.js", "custom.js");
  }

  @Test
  public void testCustomTagsArrayCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("customArray.html", "customArray.after.html", "angular.js", "custom.js");
  }

  @Test
  public void testCustomAttributesCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("customAttributes.html", "customAttributes.after.html", "angular.js", "custom.js");
  }

  @Test
  public void testCommonAttributesCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("commonAttributes.html", "commonAttributes.after.html", "angular.js", "custom.js");
  }

  @Test
  public void testCustomTagsResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("custom.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.js", resolve.getContainingFile().getName());
      assertEquals("'myCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testCustomTagsResolve15() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("custom13.after.html", "angular.js", "custom15.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("new-cus<caret>tomer", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom15.js", resolve.getContainingFile().getName());
      assertEquals("'newCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testCustomTagsViaFunctionResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("customViaFunction.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("function-cus<caret>tomer", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.js", resolve.getContainingFile().getName());
      assertEquals("'functionCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testCustomTagsViaFunctionForwardResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("customViaFunctionForward.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("great-cus<caret>tomer", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.js", resolve.getContainingFile().getName());
      assertEquals("'greatCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testCustomTagsArrayViaFunctionResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("customArrayViaFunction.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("array-cus<caret>tomer", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.js", resolve.getContainingFile().getName());
      assertEquals("'arrayCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testCustomTagsArrayResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("customArray.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("her-cus<caret>tomer", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.js", resolve.getContainingFile().getName());
      assertEquals("'herCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testOverride() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.enableInspections(CheckValidXmlInScriptBodyInspection.class);
    myFixture.configureByFiles("override.html", "angular.js");
    myFixture.checkHighlighting();
  }

  @Test
  public void testProperNamespace() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.enableInspections(RequiredAttributesInspection.class);
    myFixture.configureByFiles("namespace.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }

  @Test
  public void testCustomTagsCompletionCss() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("customCss.html", "customCss.after.html", "angular.js", "custom.js");
  }

  @Test
  public void testCustomTagsResolveCss() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("customCss.after.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.js", resolve.getContainingFile().getName());
      assertEquals("'myCustomer'", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testNoCompletionInXml() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final List<String> variants = myFixture.getCompletionVariants("standard.xml", "angular.js");
    assertThat(variants).doesNotContain("ng-form", "form", "script");
  }

  @Test
  public void testUnclosed() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("unclosed.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }

}
