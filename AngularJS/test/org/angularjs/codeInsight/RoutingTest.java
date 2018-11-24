package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.psi.*;
import com.intellij.psi.impl.include.FileIncludeProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.angularjs.codeInsight.refs.AngularJSReferencesContributor;

import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class RoutingTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "routing";
  }

  @Override
  protected boolean isWriteActionRequired() {
    return getTestName(true).contains("Completion");
  }

  public void testPartialResolve() {
    myFixture.configureByFiles("custom.js", "angular.js", "index.html", "partials/phone-details.html", "partials/phone-list.html");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("phone-<caret>details", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertInstanceOf(resolve, PsiFile.class);
    assertEquals("phone-details.html", ((PsiFile)resolve).getName());

    offsetBySignature = AngularTestUtil.findOffsetBySignature("phone-<caret>list", myFixture.getFile());
    ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    resolve = ref.resolve();
    assertInstanceOf(resolve, PsiFile.class);
    assertEquals("phone-list.html", ((PsiFile)resolve).getName());

    offsetBySignature = AngularTestUtil.findOffsetBySignature("template<caret>Id", myFixture.getFile());
    ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    resolve = ref.resolve();
    assertInstanceOf(resolve, XmlAttributeValue.class);
    assertEquals("\"templateId.htm\"", resolve.getText());
  }

  public void testNgIncludeResolve() {
    myFixture.configureByFiles("ng-include.html", "custom.js", "angular.js", "index.html");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ind<caret>ex.html", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertInstanceOf(resolve, PsiFile.class);
    assertEquals("index.html", ((PsiFile)resolve).getName());
  }

  public void testPartialCompletion() {
    myFixture.configureByFiles("custom.js", "angular.js", "index.html", "partials/phone-details.html", "partials/phone-list.html");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("<caret>partials/phone-details", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertContainsElements(variants, "partials", "index.html", "templateId.htm");
  }

  public void testControllerResolve() {
    myFixture.configureByFiles("custom.js", "angular.js", "partials/phone-details.html", "partials/phone-list.html");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("controller: 'App<caret>Ctrl'", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("'AppCtrl'", resolve.getParent().getText());
  }

  public void testControllerCompletion() {
    myFixture.configureByFiles("custom.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("controller: '<caret>AppCtrl'", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertContainsElements(variants, "AppCtrl", "OtherCtrl");
  }

  public void testControllerWithControllerAsCompletion() {
    myFixture.configureByFiles("custom1.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("controller: '<caret>AppCtrl'", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    final List<String> variants = myFixture.getLookupElementStrings();
    assertContainsElements(variants, "AppCtrl", "localCtl", "localCtl2", "OtherCtrl");
  }

  public void testControllerWithControllerAsResolve() {
    myFixture.configureByFiles("custom1.js", "angular.js", "partials/phone-details.html", "partials/phone-list.html");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("controller: 'local<caret>Ctl'", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("controller: function(){}", resolve.getParent().getText());
  }

  public void testControllerWithControllerAsResolve2() {
    myFixture.configureByFiles("custom1.js", "angular.js", "partials/phone-details.html", "partials/phone-list.html");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("controller: 'local<caret>Ctl2'", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("'localCtl2'", resolve.getParent().getText());
  }

  public void testNgAppResolve() {
    Extensions.getExtensions(FileIncludeProvider.EP_NAME);
    myFixture.configureByFiles("ngAppRouting.html", "myAppDefinition.js", "myAppUsage.js", "otherMyAppDefinition.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-app=\"my<caret>App\"", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("'myApp'", resolve.getNavigationElement().getText());
    assertEquals("myAppDefinition.js", resolve.getContainingFile().getName());

    final ResolveResult[] results = ((PsiPolyVariantReference)ref).multiResolve(false);
    assertEquals(1, results.length);
  }

  public void testNgAppCompletion() throws Exception {
    myFixture.configureByFiles("ngAppCompletion.html", "dependencyModuleDefinition.js", "myAppDefinition.js", "myAppUsage.js",
                               "otherMyAppDefinition.js", "angular.js");
    myFixture.completeBasic();
    final List<String> strings = myFixture.getLookupElementStrings();
    assertNotNull(strings);
    assertTrue(strings.contains("myApp"));
    assertTrue(strings.contains("dependency"));
  }

  public void testModuleCompletion() throws Exception {
    myFixture.configureByFiles("moduleCompletion.js", "dependencyModuleDefinition.js", "myAppDefinition.js", "myAppUsage.js",
                               "otherMyAppDefinition.js", "angular.js");
    myFixture.completeBasic();
    final List<String> strings = myFixture.getLookupElementStrings();
    assertNotNull(strings);
    assertTrue(strings.contains("myApp"));
    assertTrue(strings.contains("dependency"));
  }

  public void testModuleNavigation() throws Exception {
    myFixture.configureByFiles("moduleNavigation.js", "dependencyModuleDefinition.js", "myAppDefinition.js", "myAppUsage.js", "angular.js");
    assertModuleReferenceIsUnderCaret(true);
    final PsiReference reference = myFixture.getReferenceAtCaretPosition("moduleNavigation.js");
    assertNotNull(reference);

    PsiElement resolve = reference.resolve();
    assertNotNull(resolve);
    assertEquals("'myApp'", resolve.getNavigationElement().getText());
    assertEquals("myAppDefinition.js", resolve.getContainingFile().getName());

    final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
    assertEquals(1, results.length);
  }

  private void assertModuleReferenceIsUnderCaret(boolean value) {
    final JSLiteralExpression literal = PsiTreeUtil.getParentOfType(myFixture.getFile().findElementAt(myFixture.getCaretOffset()),
                                                                    JSLiteralExpression.class);
    assertNotNull(literal);
    final boolean accepts = AngularJSReferencesContributor.MODULE_PATTERN.accepts(literal);
    assertEquals(value, accepts);
  }

  public void testModuleDependenciesCompletion() throws Exception {
    myFixture.configureByFiles("moduleDependenciesCompletion.js", "dependencyModuleDefinition.js", "myAppDefinition.js", "myAppUsage.js",
                               "otherMyAppDefinition.js", "angular.js");
    myFixture.completeBasic();
    final List<String> strings = myFixture.getLookupElementStrings();
    assertNotNull(strings);
    assertTrue(strings.contains("myApp"));
    assertTrue(strings.contains("dependency"));
    assertTrue(strings.contains("new"));
  }

  public void testNotAModuleNoReference() throws Exception {
    myFixture.configureByFiles("notAModuleNoNavigation.js", "angular.js");
    assertModuleReferenceIsUnderCaret(true);
    final PsiReference reference = myFixture.getReferenceAtCaretPosition("notAModuleNoNavigation.js");
    assertNotNull(reference);

    final PsiElement resolve = reference.resolve();
    assertNull(resolve);
  }

  public void testModuleNavigationWithAlias() throws Exception {
    myFixture.configureByFiles("moduleNavigationWithAlias.js", "dependencyModuleDefinition.js", "myAppDefinition.js", "myAppUsage.js", "angular.js");
    assertModuleReferenceIsUnderCaret(true);
    final PsiReference reference = myFixture.getReferenceAtCaretPosition("moduleNavigationWithAlias.js");
    assertNotNull(reference);

    PsiElement resolve = reference.resolve();
    assertNotNull(resolve);
    assertEquals("'myApp'", resolve.getNavigationElement().getText());
    assertEquals("myAppDefinition.js", resolve.getContainingFile().getName());

    final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
    assertEquals(1, results.length);
  }
}
