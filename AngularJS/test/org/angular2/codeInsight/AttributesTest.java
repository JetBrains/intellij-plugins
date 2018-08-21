// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.psi.JSField;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.lang.javascript.psi.resolve.JSSimpleTypeProcessor;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.types.JSNamedType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.ThrowableRunnable;
import com.intellij.xml.util.XmlInvalidIdInspection;
import org.angularjs.AngularTestUtil;

import java.util.Arrays;

public class AttributesTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "attributes";
  }

  private static String getDirectiveDefinitionText(PsiElement resolve) {
    return resolve.getParent().getText();
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

  public void testBindingStandardCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("bindingHtml.html", "angular2.js");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "[value]");
    });
  }

  public void testTemplateReferenceDeclarations2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("variable.html", "custom.ts", "angular2.js");
      myFixture.enableInspections(RequiredAttributesInspection.class);
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testTemplateReferenceCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.html", "angular2.js");
      myFixture.completeBasic();
      myFixture.checkResultByFile("binding.after.html");
    });
  }

  public void testVariableCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("ngTemplate.html", "angular2.js");
      myFixture.completeBasic();
      myFixture.checkResultByFile("ngTemplate.after.html");
    });
  }

  public void testTemplateReferenceCompletion2Inline() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.ts", "angular2.js");
      myFixture.completeBasic();
      myFixture.checkResultByFile("binding.after.ts");
    });
  }

  public void testTemplateReferenceSmart2() throws Exception {
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

  public void testTemplateReferenceResolve2() throws Exception {
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

  public void testVariableResolve2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("ngTemplate.after.html", "angular2.js");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("my_<caret>user", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("ngTemplate.after.html", resolve.getContainingFile().getName());
      assertEquals("let-my_user", resolve.getContainingFile().findElementAt(resolve.getParent().getTextOffset()).getText());
    });
  }

  public void testTemplateReferenceResolve2Inline() throws Exception {
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

  public void testBindingCompletionViaBase2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding_via_base.html", "angular2.js", "inheritor.ts", "object.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_binding_via_base.after.html");
    });
  }

  public void testBindingResolveViaBase2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding_via_base.after.html", "angular2.js", "inheritor.ts", "object.ts");
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
      myFixture.type('\n');
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

  public void testOneTimeBindingAttributeResolve2JavaScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("compiled_binding.after.html", "angular2.js", "button.metadata.json", "button.d.ts", "color.d.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("col<caret>or", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("color.d.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, TypeScriptPropertySignature.class);
      assertEquals("color: ThemePalette", resolve.getText());
    });
  }

  public void testOneTimeBindingAttributeCompletion2JavaScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("compiled_binding.html", "angular2.js", "button.metadata.json", "button.d.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(),  "disableRipple", "color");
    });
  }

  public void testOneTimeBindingAttributeCompletion2JavaScriptPrimeButton() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("primeButton.html", "angular2.js", "primeButton.metadata.json", "primeButton.d.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(),  "icon", "iconPos", "label");
    });
  }

  public void testOneTimeBindingAttributeCompletion2ES6() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("compiled_binding.html", "angular2.js", "button.metadata.json", "button.d.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(),  "disableRipple", "color");
      assertDoesntContain(myFixture.getLookupElementStrings(),  "tabIndex");
    });
  }

  public void testBindingAttributeFunctionCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_binding.html", "angular2.js", "object_with_function.ts");
      myFixture.completeBasic();
      myFixture.type('\n');
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
      myFixture.configureByFiles("for2.html", "common.metadata.json");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ngF<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "ngFor", "[ngForOf]");
    });
  }

  public void testIfCompletion4JavascriptUmd() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("if4.html", "common.metadata.json");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("*<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "*ngIf");
      assertDoesntContain(myFixture.getLookupElementStrings(), "ngIf");
    });
  }

  public void testForTemplateCompletion2Javascript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2Template.html", "common.metadata.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "*ngFor");
    });
  }

  public void testForOfResolve2Javascript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2.html", "common.metadata.json");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ngF<caret>", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("common.metadata.json", resolve.getContainingFile().getName());
      assertEquals("\"[ngFor][ngForOf]\"", getDirectiveDefinitionText(((JSOffsetBasedImplicitElement)resolve).getElementAtOffset()));
    });
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
  public void testTemplate20JavaScript() {
    myFixture.configureByFiles("template.html", "angular2_compiled.js", "template.metadata.json");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("*myHover<caret>List", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("template.metadata.json", resolve.getContainingFile().getName());
    offsetBySignature = AngularTestUtil.findOffsetBySignature("myHover<caret>List", myFixture.getFile());
    ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    assertNull(ref.resolve());
  }

  public void testNoTemplate20JavaScript() {
    myFixture.configureByFiles("noTemplate.html", "angular2_compiled.js", "noTemplate.metadata.json");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("myHover<caret>List", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("noTemplate.metadata.json", resolve.getContainingFile().getName());
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

  public void testRouterLink() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("routerLink.html", "angular2.js", "routerLink.metadata.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "routerLink", "routerLink2");
    });
  }

  public void testComplexSelectorList() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("material.html", "angular2.js", "button.metadata.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "mat-icon-button");
    });
  }

  public void testSelectorConcatenationList() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("material.html", "angular2.js", "button.metadata.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "mat-raised-button");
    });
  }

  public void testComplexSelectorList2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("ionic.html", "angular2.js", "ionic.metadata.json");
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
      myFixture.configureByFiles("flexOrder.html", "angular2.js", "flexOrder.metadata.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "fxFlexOrder");
    });
  }

  public void testId() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.enableInspections(XmlInvalidIdInspection.class);
      myFixture.configureByFiles("id.html", "angular2.js", "object.ts");
      myFixture.checkHighlighting();
    });
  }

  public void testViewChildReferenceNavigation() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      PsiReference reference = myFixture.getReferenceAtCaretPosition("viewChildReference.ts", "angular2.js");
      assertNotNull(reference);
      PsiElement el = reference.resolve();
      assertNotNull(el);
      assertEquals("#area", el.getText());
    });
  }

  public void testViewChildReferenceContentAssist() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () ->
      assertEquals(Arrays.asList("area", "area2"),
                   myFixture.getCompletionVariants("viewChildReference.ts", "angular2.js"))
    );
  }

  public void testViewChildReferenceNavigationHTML() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      PsiReference reference =
        myFixture.getReferenceAtCaretPosition("viewChildReferenceHTML.ts", "viewChildReferenceHTML.html", "angular2.js");
      assertNotNull(reference);
      PsiElement el = reference.resolve();
      assertNotNull(el);
      assertEquals("viewChildReferenceHTML.html", el.getContainingFile().getName());
      assertEquals("#area", el.getText());
    });
  }

  public void testViewChildReferenceContentAssistHTML() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () ->
      assertEquals(Arrays.asList("area", "area2"),
                   myFixture.getCompletionVariants("viewChildReferenceHTML.ts", "viewChildReferenceHTML.html", "angular2.js"))
    );
  }

  public void testI18NAttr() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.configureByFiles("i18n.html", "angular2.js");
      myFixture.checkHighlighting(true, false, true);
    });
  }

}
