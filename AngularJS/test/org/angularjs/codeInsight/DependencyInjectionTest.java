package org.angularjs.codeInsight;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.angularjs.AngularTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.testNameFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dennis.Ushakov
 */
@TestApplication
public class DependencyInjectionTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);
  private final TestFixture<String> testNameUpper = testNameFixture(false);

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "di");
  }

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    JSTestUtils.forbidStubAstSwitch(myFixture, DependencyInjectionTest.class, testNameUpper.get());
    JSTestUtils.buildJSFileGists(myFixture.getProject());
  }

  @Test
  public void testInjectedServiceCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletionTyping("di.js", "\n", "di.after.js", "angular.js");
  }

  @Test
  public void testInjectedServiceResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("di.resolve.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("myService.fo<caret>o();", myFixture.getFile());
    ReadAction.nonBlocking(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertInstanceOf(JSProperty.class, resolve);
    }).executeSynchronously();
  }

  @Test
  public void testInjectedConstantCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletionTyping("constant.js", "\n", "constant.after.js", "angular.js");
  }

  @Test
  public void testInjectedConstantResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("constant.resolve.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("myService.fo<caret>o();", myFixture.getFile());
    ReadAction.nonBlocking(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertInstanceOf(JSProperty.class, resolve);
    }).executeSynchronously();
  }

  @Test
  public void testInjectedStaticResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("static.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("DomUtils.createSVG<caret>ElementTemplate(", myFixture.getFile());
    ReadAction.nonBlocking(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertInstanceOf(JSDefinitionExpression.class, resolve);
    }).executeSynchronously();
  }

  @Test
  public void testInjectedParameterCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("parameter.js", "parameter.after.js", "angular.js");
  }

  @Test
  public void testInjectedParameterResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("parameter.resolve.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("function (my<caret>Service)", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertInstanceOf(JSImplicitElement.class, resolve);
      assertEquals("'myService'", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testInjectedParameterToVarResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("parameterToVar.resolve.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("function (my<caret>Service)", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertInstanceOf(JSVariable.class, resolve);
    });
  }

  @Test
  public void testInjectedNamedParameterCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("namedParameter.js", "namedParameter.after.js", "angular.js");
  }

  @Test
  public void testInjectedNamedParameterResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("namedParameter.resolve.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("function (my<caret>Service)", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertInstanceOf(JSImplicitElement.class, resolve);
      assertEquals("'myService'", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  @Test
  public void testPropertyInitializedWithInjectedParameter() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.copyFileToProject("property.to.parameter.js");
    myFixture.copyFileToProject("angular.js");
    JSTestUtils.buildJSFileGists(myFixture.getProject());
    myFixture.configureByText(JavaScriptFileType.INSTANCE, "someRef.serv.<caret>");
    myFixture.completeBasic();
  }
  
  @Test
  public void testParameterNavigation() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.copyFileToProject("angular.js");
    JSTestUtils.buildJSFileGists(myFixture.getProject());
    myFixture.configureByFile(testNameUpper.get() + ".js");
    PsiElement gotoTarget = EdtTestUtil.runInEdtAndGet(() -> JSTestUtils.getGotoDeclarationTarget(myFixture));
    ReadAction.run(() -> {
      PsiElement target = gotoTarget;
      if (target instanceof LeafElement) target = target.getParent();
      assertInstanceOf(JSLiteralExpression.class, target);
    });
  }
}
