// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.include.FileIncludeIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.angularjs.AngularTestUtil;
import org.angularjs.codeInsight.refs.AngularJSReferencesContributor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dennis.Ushakov
 */
@TestApplication
public class RoutingTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "routing");
  }

  @Test
  public void testPartialResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("custom.js", "angular.js", "index.html", "partials/phone-details.html", "partials/phone-list.html");
    ReadAction.run(() -> {
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("phone-<caret>details", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertInstanceOf(PsiFile.class, resolve);
      assertEquals("phone-details.html", ((PsiFile)resolve).getName());

      offsetBySignature = AngularTestUtil.findOffsetBySignature("phone-<caret>list", myFixture.getFile());
      ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      resolve = ref.resolve();
      assertInstanceOf(PsiFile.class, resolve);
      assertEquals("phone-list.html", ((PsiFile)resolve).getName());

      offsetBySignature = AngularTestUtil.findOffsetBySignature("template<caret>Id", myFixture.getFile());
      ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      resolve = ref.resolve();
      assertInstanceOf(XmlAttributeValue.class, resolve);
      assertEquals("\"templateId.htm\"", resolve.getText());
    });
  }

  @Test
  public void testNgIncludeResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ng-include.html", "custom.js", "angular.js", "index.html");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ind<caret>ex.html", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertInstanceOf(PsiFile.class, resolve);
      assertEquals("index.html", ((PsiFile)resolve).getName());
    });
  }

  @Test
  public void testPartialCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("custom.js", "angular.js", "index.html", "partials/phone-details.html", "partials/phone-list.html");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("<caret>partials/phone-details", myFixture.getFile());
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature));
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertThat(variants).contains("partials", "index.html", "templateId.htm");
  }

  @Test
  public void testControllerResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("custom.js", "angular.js", "partials/phone-details.html", "partials/phone-list.html");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("controller: 'App<caret>Ctrl'", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("'AppCtrl'", resolve.getParent().getText());
    });
  }

  @Test
  public void testControllerCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("custom.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("controller: '<caret>AppCtrl'", myFixture.getFile());
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature));
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertThat(variants).contains("AppCtrl", "OtherCtrl");
  }

  @Test
  public void testControllerWithControllerAsCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("custom1.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("controller: '<caret>AppCtrl'", myFixture.getFile());
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature));
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertThat(variants).contains("AppCtrl", "localCtl", "localCtl2", "OtherCtrl");
  }

  @Test
  public void testControllerWithControllerAsResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("custom1.js", "angular.js", "partials/phone-details.html", "partials/phone-list.html");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("controller: 'local<caret>Ctl'", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("controller: function(){}", resolve.getParent().getText());
    });
  }

  @Test
  public void testControllerWithControllerAsResolve2() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("custom1.js", "angular.js", "partials/phone-details.html", "partials/phone-list.html");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("controller: 'local<caret>Ctl2'", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("'localCtl2'", resolve.getParent().getText());
    });
  }

  @Test
  public void testNgAppResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    FileIncludeIndex.FILE_INCLUDE_PROVIDER_EP_NAME.getExtensionList();
    myFixture.configureByFiles("ngAppRouting.html", "myAppDefinition.js", "myAppUsage.js", "otherMyAppDefinition.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-app=\"my<caret>App\"", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("'myApp'", resolve.getNavigationElement().getText());
      assertEquals("myAppDefinition.js", resolve.getContainingFile().getName());

      final ResolveResult[] results = ((PsiPolyVariantReference)ref).multiResolve(false);
      assertEquals(1, results.length);
    });
  }

  @Test
  public void testNgAppCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngAppCompletion.html", "dependencyModuleDefinition.js", "myAppDefinition.js", "myAppUsage.js",
                               "otherMyAppDefinition.js", "angular.js");
    myFixture.completeBasic();
    final List<String> strings = myFixture.getLookupElementStrings();
    assertNotNull(strings);
    assertTrue(strings.contains("myApp"));
    assertTrue(strings.contains("dependency"));
  }

  @Test
  public void testModuleCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("moduleCompletion.js", "dependencyModuleDefinition.js", "myAppDefinition.js", "myAppUsage.js",
                               "otherMyAppDefinition.js", "angular.js");
    myFixture.completeBasic();
    final List<String> strings = myFixture.getLookupElementStrings();
    assertNotNull(strings);
    assertTrue(strings.contains("myApp"));
    assertTrue(strings.contains("dependency"));
  }

  @Test
  public void testModuleNavigation() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("moduleNavigation.js", "dependencyModuleDefinition.js", "myAppDefinition.js", "myAppUsage.js", "angular.js");
    assertModuleReferenceIsUnderCaret(true);
    final PsiReference reference = myFixture.getReferenceAtCaretPosition("moduleNavigation.js");
    assertNotNull(reference);

    ReadAction.run(() -> {
      PsiElement resolve = reference.resolve();
      assertNotNull(resolve);
      assertEquals("'myApp'", resolve.getNavigationElement().getText());
      assertEquals("myAppDefinition.js", resolve.getContainingFile().getName());

      final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
      assertEquals(1, results.length);
    });
  }

  private void assertModuleReferenceIsUnderCaret(boolean value) {
    CodeInsightTestFixture myFixture = codeInsight.get();
    ReadAction.run(() -> {
      final JSLiteralExpression literal = PsiTreeUtil.getParentOfType(myFixture.getFile().findElementAt(myFixture.getCaretOffset()),
                                                                      JSLiteralExpression.class);
      assertNotNull(literal);
      final boolean accepts = AngularJSReferencesContributor.MODULE_PATTERN.accepts(literal);
      assertEquals(value, accepts);
    });
  }

  @Test
  public void testModuleDependenciesCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("moduleDependenciesCompletion.js", "dependencyModuleDefinition.js", "myAppDefinition.js", "myAppUsage.js",
                               "otherMyAppDefinition.js", "angular.js");
    myFixture.completeBasic();
    final List<String> strings = myFixture.getLookupElementStrings();
    assertNotNull(strings);
    assertTrue(strings.contains("myApp"));
    assertTrue(strings.contains("dependency"));
    assertTrue(strings.contains("new"));
  }

  @Test
  public void testNotAModuleNoReference() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("notAModuleNoNavigation.js", "angular.js");
    assertModuleReferenceIsUnderCaret(true);
    final PsiReference reference = myFixture.getReferenceAtCaretPosition("notAModuleNoNavigation.js");
    assertNotNull(reference);

    ReadAction.nonBlocking(() -> {
      final PsiElement resolve = reference.resolve();
      assertNull(resolve);
    }).executeSynchronously();
  }

  @Test
  public void testModuleNavigationWithAlias() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("moduleNavigationWithAlias.js", "dependencyModuleDefinition.js", "myAppDefinition.js", "myAppUsage.js", "angular.js");
    assertModuleReferenceIsUnderCaret(true);
    final PsiReference reference = myFixture.getReferenceAtCaretPosition("moduleNavigationWithAlias.js");
    assertNotNull(reference);

    ReadAction.run(() -> {
      PsiElement resolve = reference.resolve();
      assertNotNull(resolve);
      assertEquals("'myApp'", resolve.getNavigationElement().getText());
      assertEquals("myAppDefinition.js", resolve.getContainingFile().getName());

      final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
      assertEquals(1, results.length);
    });
  }

  @Test
  public void testModuleDefinitionWrapperTopLevel() {
    final String pr = "wrapperTopLevel/";
    checkModuleDefinitionWrapper(pr, "");
  }

  @Test
  public void testModuleDefinitionWrapperGlobal() {
    final String pr = "wrapperGlobal/";
    checkModuleDefinitionWrapper(pr, "ApplicationConfiguration.");
  }

  private void checkModuleDefinitionWrapper(String pr, String declarationQualifier) {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles(pr + "usage.es6", pr + "declaration.es6", pr + "wrapper.es6", "angular.js");
    assertModuleReferenceIsUnderCaret(true);
    final PsiReference reference = myFixture.getReferenceAtCaretPosition(pr + "usage.es6");
    assertNotNull(reference);

    ReadAction.run(() -> {
      PsiElement resolve = reference.resolve();
      assertNotNull(resolve);
      assertEquals(declarationQualifier + "registerModule", resolve.getNavigationElement().getText());
      assertTrue(resolve.getParent() instanceof JSCallExpression);
      assertEquals("discoverability", StringUtil.unquoteString(((JSCallExpression)resolve.getParent()).getArguments()[0].getText()));
      assertEquals("declaration.es6", resolve.getContainingFile().getName());

      final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
      assertEquals(1, results.length);
    });
  }
}
