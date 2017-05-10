package org.angularjs.codeInsight;

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection;
import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.resolve.JSSimpleTypeProcessor;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.JSNamedType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.ThrowableRunnable;
import org.angularjs.AngularTestUtil;

/**
 * @author Dennis.Ushakov
 */
public class AttributesTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "attributes";
  }

  private static String getDirectiveDefinitionText(PsiElement resolve) {
    return resolve.getParent().getText();
  }

  public void testStandardAttributesCompletion() {
    myFixture.testCompletion("standard.html", "standard.after.html", "angular.js");
  }

  public void testNgInclude() {
    myFixture.testCompletion("ng-include.html", "ng-include.after.html", "angular.js");
  }

  public void testStandardAttributesResolve() {
    myFixture.configureByFiles("standard.after.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-mo<caret>del", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("angular.js", resolve.getContainingFile().getName());
  }

  public void testStandardAttributesResolveOldStyle() {
    myFixture.configureByFiles("standard.after.html", "angular12.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-mo<caret>del", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("angular12.js", resolve.getContainingFile().getName());
  }

  public void testStandardAttributesDataResolve() {
    myFixture.configureByFiles("standard-data.after.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-mo<caret>del", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("angular.js", resolve.getContainingFile().getName());
  }

  public void testCustomAttributesInDirectiveCompletion() {
    myFixture.testCompletion("customInDirective.html", "customInDirective.after.html", "custom.js", "angular.js");
  }

  public void testCustomAttributesInDirectiveResolve() {
    myFixture.configureByFiles("customInDirective.after.html", "custom.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'myCustomer'", getDirectiveDefinitionText(resolve));
  }

  public void testCustomAttributesInDirectiveEmptyCompletion() {
    myFixture.testCompletion("customInDirectiveEmpty.html", "customInDirectiveEmpty.after.html", "custom.js", "angular.js");
  }

  public void testCustomAttributesInDirectiveEmptyResolve() {
    myFixture.configureByFiles("customInDirectiveEmpty.after.html", "custom.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'myCustomer'", getDirectiveDefinitionText(resolve));
  }

  public void testCustomAttributesCompletion() {
    myFixture.testCompletion("custom.html", "custom.after.html", "custom.js");
  }

  public void testCustomAttributesTemplateCompletion() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        (ThrowableRunnable<Exception>)() -> myFixture.testCompletion("custom.html", "custom.after.html", "custom_template.js"));
  }

  public void testCustomAttributesCompletion15() {
    myFixture.testCompletion("custom.html", "custom.after.html", "custom15.js");
  }

  public void testCustomAttributesCompletion20TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        (ThrowableRunnable<Exception>)() -> myFixture.testCompletion("custom2.html", "custom2.after.html", "custom.ts"));
  }

  public void testCustomAttributesCompletion20JavaScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(),
                                        (ThrowableRunnable<Exception>)() -> myFixture.testCompletion("custom2.html", "custom2.after.html", "custom2.js"));
  }

  public void testCustomAttributesResolve() {
    myFixture.configureByFiles("custom.after.html", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'myCustomer'", getDirectiveDefinitionText(resolve));
  }

  public void testCustomAttributesTemplateResolve() {
    myFixture.configureByFiles("custom.after.html", "custom_template.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom_template.js", resolve.getContainingFile().getName());
    assertEquals("`myCustomer`", getDirectiveDefinitionText(resolve));
  }

  public void testCustomAttributesResolve15() {
    myFixture.configureByFiles("custom.after.html", "custom15.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom15.js", resolve.getContainingFile().getName());
    assertEquals("'myCustomer'", getDirectiveDefinitionText(resolve));
  }

  public void testCustomAttributesResolve20TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom2.after.html", "custom.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.ts", resolve.getContainingFile().getName());
      assertEquals("Directive({\n" +
                   "    selector: '[my-customer]',\n" +
                   "    properties: {\n" +
                   "        'id':'dependency'\n" +
                   "    },\n" +
                   "    templateUrl: '',\n" +
                   "    styleUrls: [''],\n" +
                   "})", getDirectiveDefinitionText(resolve));
    });
  }

  public void testCustomAttributesResolve20JavaScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom2.after.html", "custom2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom2.js", resolve.getContainingFile().getName());
      assertEquals("new angular.DirectiveAnnotation({\n" +
                   "    selector: '[my-customer]'\n" +
                   "  })", getDirectiveDefinitionText(resolve));
    });
  }

  public void testControllerCompletion() {
    myFixture.testCompletionTyping("controller.html", "\n", "controller.after.html", "custom.js", "angular.js");
  }

  public void testControllerResolve() {
    myFixture.configureByFiles("controller.resolve.html", "custom.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("Supa<caret>Controller", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'SupaController'", getDirectiveDefinitionText(resolve));
  }

  public void testPrefixedControllerResolve() {
    myFixture.configureByFiles("controller.prefixed.html", "custom.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("Supa<caret>Controller", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'SupaController'", getDirectiveDefinitionText(resolve));
  }

  public void testAppCompletion() {
    myFixture.testCompletion("app.html", "app.after.html", "custom.js", "angular.js");
  }

  public void testAppResolve() {
    myFixture.configureByFiles("app.after.html", "custom.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("docs<caret>SimpleDirective", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("'docsSimpleDirective'", getDirectiveDefinitionText(resolve));
  }

  public void testNormalization() {
    myFixture.configureByFiles("normalize.html", "angular.js");
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    myFixture.enableInspections(XmlUnboundNsPrefixInspection.class);
    myFixture.checkHighlighting();
  }

  public void testNgSrc() {
    myFixture.configureByFiles("ng-src.html", "angular.js");
    myFixture.enableInspections(RequiredAttributesInspection.class);
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    myFixture.checkHighlighting();
  }

  public void testSrcBinding20() {
    myFixture.configureByFiles("srcBinding.html", "angular2.js");
    myFixture.enableInspections(RequiredAttributesInspection.class);
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    myFixture.checkHighlighting();
  }

  public void testEventHandlers2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("event.html", "angular2.js");
      myFixture.enableInspections(RequiredAttributesInspection.class);
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testEventHandlersStandardCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("event.html", "angular2.js");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "(mouseover)");
    });
  }

  public void testVariableDeclarations2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("variable.html", "custom.ts", "angular2.js");
      myFixture.enableInspections(RequiredAttributesInspection.class);
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testVariableCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.html", "angular2.js");
      myFixture.completeBasic();
      myFixture.checkResultByFile("binding.after.html");
    });
  }

  public void testVariableCompletion2Inline() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.ts", "angular2.js");
      myFixture.completeBasic();
      myFixture.checkResultByFile("binding.after.ts");
    });
  }

  public void testVariableSmart2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.type.html", "angular2.js");
      final PsiFile file = myFixture.getFile();
      final int offset = AngularTestUtil.findOffsetBySignature("user<caret>name,", file);
      final JSReferenceExpression ref = PsiTreeUtil.getParentOfType(file.findElementAt(offset), JSReferenceExpression.class);
      final JSSimpleTypeProcessor processor = new JSSimpleTypeProcessor();
      JSTypeEvaluator.evaluateTypes(ref, file, processor);
      final JSType type = processor.getType();
      assertInstanceOf(type, JSNamedType.class);
      assertEquals("HTMLInputElement", type.getTypeText());
    });
  }

  public void testVariableResolve2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.after.html", "angular2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("user<caret>name", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("binding.after.html", resolve.getContainingFile().getName());
      assertEquals("#username", resolve.getContainingFile().findElementAt(resolve.getParent().getTextOffset()).getText());
    });
  }

  public void testVariableResolve2Inline() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.after.ts", "angular2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("in<caret>put_el.", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("binding.after.ts", resolve.getContainingFile().getName());
      assertEquals("#input_el", resolve.getContainingFile().findElementAt(resolve.getParent().getTextOffset()).getText());
    });
  }

  public void testBindingCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding.html", "angular2.js", "object.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_binding.after.html");
    });
  }

  public void testBindingResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding.after.html", "angular2.js", "object.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("[mod<caret>el]", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testBindingOverride2CompletionTypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding.html", "angular2.js", "objectOverride.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_binding.after.html");
    });
  }

  public void testBindingOverrideResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding.after.html", "angular2.js", "objectOverride.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("[mod<caret>el]", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("objectOverride.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }
  public void testBindingAttributeCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_binding.html", "angular2.js", "object.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("attribute_binding.after.html");
    });
  }

  public void testBindingAttributeResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_binding.after.html", "angular2.js", "object.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("[mod<caret>el]", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testOneTimeBindingAttributeCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_one_time_binding.html", "angular2.js", "object.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "model", "oneTime", "oneTimeList");
    });
  }

  public void testOneTimeBindingAttributeResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_one_time_binding.after.html", "angular2.js", "object.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("one<caret>Time", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testOneTimeBindingAttributeCompletion2JavaScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("compiled_binding.html", "angular2.js", "object.js");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "disableRipple", "color");
    });
  }

  public void testOneTimeBindingAttributeResolve2JavaScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("compiled_binding.after.html", "angular2.js", "object.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("col<caret>or", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("object.js", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSImplicitElement.class);
      assertEquals("\"color\"", getDirectiveDefinitionText(resolve));
    });
  }

  public void testOneTimeBindingAttributeCompletion2JavaScriptUmd() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("compiled_binding.html", "angular2.js", "object.umd.js");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(),  "disableRipple", "color");
    });
  }

  public void testOneTimeBindingAttributeResolve2JavaScriptUmd() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("compiled_binding.after.html", "angular2.js", "object.umd.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("col<caret>or", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("object.umd.js", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSImplicitElement.class);
      assertEquals("\"color\"", getDirectiveDefinitionText(resolve));
    });
  }

  public void testBindingAttributeFunctionCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_binding.html", "angular2.js", "object_with_function.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("attribute_binding.after.html");
    });
  }

  public void testBindingAttributeFunctionResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_binding.after.html", "angular2.js", "object_with_function.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("[mod<caret>el]", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("object_with_function.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSFunction.class);
    });
  }

  public void testEventHandlerCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_event.html", "angular2.js", "object.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_event.after.html");
    });
  }

  public void testEventHandlerResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_event.after.html", "angular2.js", "object.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("(co<caret>mplete)", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testEventHandlerOverrideCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_event.html", "angular2.js", "objectOverride.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_event.after.html");
    });
  }

  public void testEventHandlerOverrideResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_event.after.html", "angular2.js", "objectOverride.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("(co<caret>mplete)", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("objectOverride.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testNgSrcCompletion() {
    myFixture.configureByFiles("ng-src.completion.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("img ng-<caret>", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "ng-src");

    offsetBySignature = AngularTestUtil.findOffsetBySignature("div ng-<caret>", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    assertDoesntContain(myFixture.getLookupElementStrings(), "ng-src");
  }

  public void testRestrictE() {
    myFixture.configureByFiles("form.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("div f<caret>", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    assertDoesntContain(myFixture.getLookupElementStrings(), "form");
  }

  public void testIncorrectJSDoc() {
    myFixture.configureByText(System.currentTimeMillis() + ".js",
                              "/**\n" +
                              " * @ngdoc directive\n" +
                              " * @name yaSelect\n" +
                              " * @restrict E\n" +
                              " *\n" +
                              " * @param description\n" +
                              " *\n" +
                              " * @description Р’С‹РІРѕРґРёС‚ select\n" +
                              " *\n" +
                              " * @param ngModel Assignable angular expression to data-bind to. sa\n" +
                              " * bla bla bla l\n" +
                              " */");
    myFixture.doHighlighting();
  }

  public void testInlineStyle() {
    myFixture.configureByFiles("style.html", "angular.js");
    myFixture.checkHighlighting();
  }

  public void testElement() {
    myFixture.configureByFiles("ng-copy.html", "angular.js");
    for (String signature : new String[]{"input", "select", "textarea", "a"}) {
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("<" + signature + " ng-<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "ng-copy");
    }
  }

  public void testForCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2.html", "angular2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ngF<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "ngFor", "[ngForOf]");
    });
  }

  public void testForOfResolve2Typescript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2.html", "angular2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ngF<caret>", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("angular2.js", resolve.getContainingFile().getName());
      assertEquals("Directive({selector: '[ngFor][ngForOf]', properties: ['ngForOf'], lifecycle: [onCheck]})", getDirectiveDefinitionText(resolve));
    });
  }

  public void testForCompletion2Javascript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2.html", "angular2_compiled.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ngF<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "ngFor", "[ngForOf]");
    });
  }

  public void testIfCompletion4Javascript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("if4.html", "angular4_compiled.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("*<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "*ngIf");
      assertDoesntContain(myFixture.getLookupElementStrings(), "ngIf");
    });
  }

  public void testIfCompletion4JavascriptUmd() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("if4.html", "angular4_compiled.umd.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("*<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "*ngIf");
      assertDoesntContain(myFixture.getLookupElementStrings(), "ngIf");
    });
  }

  public void testIfCompletion4JavascriptEs6InEs5() throws Exception {
    myFixture.configureByFiles("if4.html", "angular4_compiled.es6.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("*<caret>", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "*ngIf");
    assertDoesntContain(myFixture.getLookupElementStrings(), "ngIf");
  }

  public void testForTemplateCompletion2Javascript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2Template.html", "angular2_compiled.js");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "*ngFor");
    });
  }

  public void testForOfResolve2Javascript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2.html", "angular2_compiled.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ngF<caret>", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("angular2_compiled.js", resolve.getContainingFile().getName());
      assertEquals("args: [{ selector: '[ngFor][ngForOf]', inputs: ['ngForTrackBy', 'ngForOf', 'ngForTemplate'] },]",
                   getDirectiveDefinitionText(resolve));
    });
  }

  public void testInputElement() {
    myFixture.configureByFiles("ng-disabled.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("<button ng-<caret>", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "ng-disabled");
  }

  public void testInputWithParent() {
    myFixture.configureByFiles("ng-disabled-parent.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("<button ng-<caret>", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "ng-disabled");
  }

  public void testInputWithNgForm() {
    myFixture.configureByFiles("ng-disabled-ng-form.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("<button ng-<caret>", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "ng-disabled");
  }

  public void testRepeatCompletion() {
    myFixture.configureByFiles("ng-repeat.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("<div ng-rep<caret>", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "ng-repeat", "ng-repeat-start", "ng-repeat-end");
  }

  public void testRepeatResolve() {
    myFixture.configureByFiles("ng-repeat.resolve.html", "angular.js");
    for (String suffix : new String[]{"", "-start", "-end"}) {
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng<caret>-repeat" + suffix, myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("angular.js", resolve.getContainingFile().getName());
    }
  }

  public void testTemplateUrl20Completion() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.ts", "angular2.js", "custom.html");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("templateUrl: '<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "custom.ts", "angular2.js", "custom.html");
    });
  }

  public void testTemplateUrl20Resolve() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.template.ts", "angular2.js", "custom.html");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("templateUrl: '<caret>", myFixture.getFile());
      final PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      final PsiElement resolve = ref.resolve();
      assertInstanceOf(resolve, PsiFile.class);
      assertEquals("custom.html", ((PsiFile)resolve).getName());
    });
  }

  public void testStyleUrls20Completion() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.ts", "angular2.js", "custom.html");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("styleUrls: ['<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "custom.ts", "angular2.js", "custom.html");
    });
  }


  public void testStyleUrls20Resolve() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.style.ts", "angular2.js", "custom.html");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("styleUrls: ['<caret>", myFixture.getFile());
      final PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      final PsiElement resolve = ref.resolve();
      assertInstanceOf(resolve, PsiFile.class);
      assertEquals("custom.html", ((PsiFile)resolve).getName());
    });
  }

  public void testTemplate20TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("template.html", "angular2.js", "template.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("*myHover<caret>List", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("template.ts", resolve.getContainingFile().getName());
      offsetBySignature = AngularTestUtil.findOffsetBySignature("myHover<caret>List", myFixture.getFile());
      ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      assertNull(ref.resolve());
    });
  }

  public void testNoTemplate20TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("noTemplate.html", "angular2.js", "noTemplate.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("myHover<caret>List", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("noTemplate.ts", resolve.getContainingFile().getName());
      offsetBySignature = AngularTestUtil.findOffsetBySignature("*myHover<caret>List", myFixture.getFile());
      ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      assertNull(ref.resolve());
    });
  }
  public void testTemplate20JavaScript() throws Exception {
    myFixture.configureByFiles("template.html", "angular2_compiled.js", "template.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("*myHover<caret>List", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("template.js", resolve.getContainingFile().getName());
    offsetBySignature = AngularTestUtil.findOffsetBySignature("myHover<caret>List", myFixture.getFile());
    ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    assertNull(ref.resolve());
  }

  public void testNoTemplate20JavaScript() throws Exception {
    myFixture.configureByFiles("noTemplate.html", "angular2_compiled.js", "noTemplate.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("myHover<caret>List", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("noTemplate.js", resolve.getContainingFile().getName());
    offsetBySignature = AngularTestUtil.findOffsetBySignature("*myHover<caret>List", myFixture.getFile());
    ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    assertNull(ref.resolve());
  }

  public void testBindingNamespace() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("bindingNamespace.html", "angular2.js");
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.enableInspections(XmlUnboundNsPrefixInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testEventNamespace() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("eventNamespace.html", "angular2.js");
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.enableInspections(XmlUnboundNsPrefixInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testCssExternalReference20() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("cssExtRef.ts", "angular2.js", "css.css");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("inDa<caret>Class", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("css.css", resolve.getContainingFile().getName());
    });
  }

  public void testCssInternalReference20() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("cssIntRef.ts", "angular2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("inDa<caret>Class", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
    });
  }

  public void testCssInternalReferenceWithHtmlTag20() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("cssIntRefHtmlTag.ts", "angular2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("inDa<caret>Class", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
    });
  }

  public void testCaseCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("case.html", "angular2.js");
      myFixture.completeBasic();
      myFixture.type('\n');
      myFixture.checkResultByFile("case.after.html");
    });
  }

  public void testSrcInjection() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("srcInjection.html", "angular.js");
      myFixture.enableInspections(HtmlUnknownTargetInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testRouterLink() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("routerLink.html", "angular2.js", "routerLink.js");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "routerLink", "routerLink2");
    });
  }

  public void testComplexSelectorList() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("material.html", "angular2.js", "material.js");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "md-icon-button", "mat-icon-button");
    });
  }

  public void testSelectorConcatenationList() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("material.html", "angular2.js", "material2.js");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "md-raised-button", "mat-raised-button");
    });
  }

  public void testComplexSelectorList2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("ionic.html", "angular2.js", "ionic.js");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "ion-item");
    });
  }

  public void testSelectorListSpaces() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("spaces.html", "angular2.js", "spaces.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "other-attr");
    });
  }

  public void testSelectorListSpaces2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("spaces.html", "angular2.js", "spaces.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "other-attr");
    });
  }

  public void testSelectorListSpacesCompiled() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("flexOrder.html", "angular2.js", "flexOrder.js");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "fxFlexOrder");
    });
  }

  public void testComponent15AttributesCompletion() {
    myFixture.testCompletion("component15.html", "component15.after.html", "angular.js", "component15.js");
  }

  public void testComponent15AttributesResolve() {
    myFixture.configureByFiles("component15.after.html", "angular.js", "component15.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("he<caret>ro=", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("component15.js", resolve.getContainingFile().getName());
    assertEquals("hero: '<'", getDirectiveDefinitionText(resolve));
  }
}
