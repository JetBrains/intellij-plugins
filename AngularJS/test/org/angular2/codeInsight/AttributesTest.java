// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.inspections.JSCheckFunctionSignaturesInspection;
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection;
import com.intellij.lang.javascript.psi.JSField;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature;
import com.intellij.lang.javascript.psi.resolve.JSSimpleTypeProcessor;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.types.JSNamedType;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.TestLookupElementPresentation;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.util.XmlInvalidIdInspection;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.html.psi.Angular2HtmlReferenceVariable;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.intellij.openapi.util.Pair.pair;
import static java.util.Arrays.asList;
import static org.angularjs.AngularTestUtil.configureWithMetadataFiles;

public class AttributesTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "attributes";
  }

  @NotNull
  private PsiElement resolveReference(@NotNull String signature) {
    return AngularTestUtil.resolveReference(signature, myFixture);
  }

  private void assertUnresolvedReference(@NotNull String signature) {
    AngularTestUtil.assertUnresolvedReference(signature, myFixture);
  }

  public void testSrcBinding20() {
    myFixture.configureByFiles("srcBinding.html", "package.json");
    myFixture.enableInspections(RequiredAttributesInspection.class);
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    myFixture.checkHighlighting();
  }

  public void testEventHandlers2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("event.html", "package.json");
      myFixture.enableInspections(RequiredAttributesInspection.class);
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testEventHandlersStandardCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("event.html", "package.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "(mouseover)");
    });
  }

  public void testBindingStandardCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("bindingHtml.html", "package.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "[value]");
    });
  }

  public void testTemplateReferenceDeclarations2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("variable.html", "custom.ts", "package.json");
      myFixture.enableInspections(RequiredAttributesInspection.class);
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testTemplateReferenceCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.html", "package.json");
      myFixture.completeBasic();
      myFixture.checkResultByFile("binding.after.html");
    });
  }

  public void testVariableCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("ngTemplate.html", "package.json");
      myFixture.completeBasic();
      myFixture.checkResultByFile("ngTemplate.after.html");
    });
  }

  public void testTemplateReferenceCompletion2Inline() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.ts", "package.json");
      myFixture.completeBasic();
      myFixture.checkResultByFile("binding.after.ts");
    });
  }

  public void testTemplateReferenceSmart2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.type.html", "package.json");
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
      myFixture.configureByFiles("binding.after.html", "package.json");
      PsiElement resolve = resolveReference("$event, user<caret>name");
      assertInstanceOf(resolve, Angular2HtmlReferenceVariable.class);
      assertEquals("binding.after.html", resolve.getContainingFile().getName());
      assertEquals("#username", resolve.getParent().getParent().getText());
    });
  }

  public void testVariableResolve2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("ngTemplate.after.html", "package.json");
      PsiElement resolve = resolveReference("let-my_<caret>user");
      assertEquals("ngTemplate.after.html", resolve.getContainingFile().getName());
      assertEquals("let-my_user", resolve.getContainingFile().findElementAt(resolve.getParent().getTextOffset()).getText());

      PsiElement resolve2 = resolveReference("{{my_<caret>user");
      assertEquals("ngTemplate.after.html", resolve2.getContainingFile().getName());
      assertEquals("let-my_user", resolve2.getContainingFile().findElementAt(resolve2.getParent().getTextOffset()).getText());
    });
  }

  public void testTemplateReferenceResolve2Inline() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.after.ts", "package.json");
      PsiElement resolve = resolveReference("in<caret>put_el.");
      assertInstanceOf(resolve, Angular2HtmlReferenceVariable.class);
      assertEquals("binding.after.ts", resolve.getContainingFile().getName());
      assertEquals("#input_el", resolve.getParent().getParent().getText());
    });
  }

  public void testBindingCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding.html", "package.json", "object.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_binding.after.html");
    });
  }

  public void testBindingResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding.after.html", "package.json", "object.ts");
      PsiElement resolve = resolveReference("[mod<caret>el]");
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testBindingResolve2TypeScriptInputInDecorator() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding.after.html", "package.json", "object_in_dec.ts");
      PsiElement resolve = resolveReference("[mod<caret>el]");
      assertEquals("object_in_dec.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);

      TypeScriptClass cls = PsiTreeUtil.getContextOfType(resolve, TypeScriptClass.class);
      assert cls != null;
      Angular2Directive component = Angular2EntitiesProvider.getComponent(cls);
      assert component != null;
      assertEquals(ContainerUtil.newHashSet("model", "id", "oneTime", "oneTimeList"),
                   component.getInputs().stream().map(Angular2DirectiveProperty::getName).collect(Collectors.toSet()));
      assertEquals(Collections.singleton("complete"),
                   component.getOutputs().stream().map(Angular2DirectiveProperty::getName).collect(Collectors.toSet()));
    });
  }

  public void testBindingCompletionViaBase2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding_via_base.html", "package.json", "inheritor.ts", "object.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_binding_via_base.after.html");
    });
  }

  public void testBindingResolveViaBase2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding_via_base.after.html", "package.json", "inheritor.ts", "object.ts");
      PsiElement resolve = resolveReference("[mod<caret>el]");
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testBindingOverride2CompletionTypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding.html", "package.json", "objectOverride.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_binding.after.html");
    });
  }

  public void testBindingOverrideResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding.after.html", "package.json", "objectOverride.ts");
      PsiElement resolve = resolveReference("[mod<caret>el]");
      assertEquals("objectOverride.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testBindingAttributeCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_binding.html", "package.json", "object.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("attribute_binding.after.html");
    });
  }

  public void testBindingAttributeResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_binding.after.html", "package.json", "object.ts");
      PsiElement resolve = resolveReference("[mod<caret>el]");
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testOneTimeBindingAttributeCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_one_time_binding.html", "package.json", "object.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "model", "oneTime", "oneTimeList");
    });
  }

  public void testOneTimeBindingAttributeResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_one_time_binding.after.html", "package.json", "object.ts");
      PsiElement resolve = resolveReference("one<caret>Time");
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testOneTimeBindingAttributeResolve2JavaScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "button");
      myFixture.configureByFiles("compiled_binding.after.html", "button.d.ts", "color.d.ts");
      PsiElement resolve = resolveReference("col<caret>or");
      assertEquals("color.d.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, TypeScriptPropertySignature.class);
      assertEquals("color: ThemePalette", resolve.getText());
    });
  }

  public void testOneTimeBindingAttributeCompletion2JavaScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "button");
      myFixture.configureByFiles("compiled_binding.html", "color.d.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "color");
    });
  }

  public void testOneTimeBindingAttributeCompletion2JavaScriptPrimeButton() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "primeButton");
      myFixture.configureByFiles("primeButton.html");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "icon", "iconPos", "label");
    });
  }

  public void testOneTimeBindingAttributeCompletion2ES6() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "button");
      myFixture.configureByFiles("compiled_binding.html", "color.d.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "color");
      assertDoesntContain(myFixture.getLookupElementStrings(), "tabIndex");
    });
  }

  public void testBindingAttributeFunctionCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_binding.html", "package.json", "object_with_function.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("attribute_binding.after.html");
    });
  }

  public void testBindingAttributeFunctionResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_binding.after.html", "package.json", "object_with_function.ts");
      PsiElement resolve = resolveReference("[mod<caret>el]");
      assertEquals("object_with_function.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSFunction.class);
    });
  }

  public void testEventHandlerCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_event.html", "package.json", "object.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_event.after.html");
    });
  }

  public void testEventHandlerResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_event.after.html", "package.json", "object.ts");
      PsiElement resolve = resolveReference("(co<caret>mplete)");
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testEventHandlerOverrideCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_event.html", "package.json", "objectOverride.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_event.after.html");
    });
  }

  public void testEventHandlerOverrideResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_event.after.html", "package.json", "objectOverride.ts");
      PsiElement resolve = resolveReference("(co<caret>mplete)");
      assertEquals("objectOverride.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testForCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2.html", "ng_for_of.ts", "package.json");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ngF<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "ngFor", "[ngForOf]");
    });
  }

  public void testForOfResolve2Typescript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2.html", "ng_for_of.ts", "package.json");
      PsiElement resolve = resolveReference("ngF<caret>");
      assertEquals("ng_for_of.ts", resolve.getContainingFile().getName());
      assertEquals("ngFor", resolve.getText());
      assertEquals("@Directive({selector: '[ngFor][ngForOf]'})", AngularTestUtil.getDirectiveDefinitionText(resolve));
    });
  }

  public void testForCompletion2Javascript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "common");
      myFixture.configureByFiles("for2.html");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ngF<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "ngFor", "[ngForOf]");
    });
  }

  public void testIfCompletion4JavascriptUmd() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "common");
      myFixture.configureByFiles("if4.html");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("*<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "*ngIf");
      assertDoesntContain(myFixture.getLookupElementStrings(), "ngIf");
    });
  }

  public void testForTemplateCompletion2Javascript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "common");
      myFixture.configureByFiles("for2Template.html");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "*ngFor");
    });
  }

  public void testForOfResolve2Javascript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "common");
      myFixture.configureByFiles("for2.html");
      PsiElement resolve = resolveReference("ngF<caret>");
      assertEquals("common.metadata.json", resolve.getContainingFile().getName());
      assertEquals(
        "NgForOf <metadata template>: selector=[ngFor][ngForOf]; inputs=[ngForOf, ngForTrackBy, ngForTemplate]; outputs=[]; inOuts=[]",
        resolve.getParent().toString());
    });
  }

  public void testTemplateUrl20Completion() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.ts", "package.json", "custom.html");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("templateUrl: '<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "custom.ts", "package.json", "custom.html");
    });
  }

  public void testTemplateUrl20Resolve() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.template.ts", "package.json", "custom.html");
      PsiElement resolve = resolveReference("templateUrl: '<caret>");
      assertInstanceOf(resolve, PsiFile.class);
      assertEquals("custom.html", ((PsiFile)resolve).getName());
    });
  }

  public void testStyleUrls20Completion() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.ts", "package.json", "custom.html");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("styleUrls: ['<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "custom.ts", "package.json", "custom.html");
    });
  }


  public void testStyleUrls20Resolve() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.style.ts", "package.json", "custom.html");
      PsiElement resolve = resolveReference("styleUrls: ['<caret>");
      assertInstanceOf(resolve, PsiFile.class);
      assertEquals("custom.html", ((PsiFile)resolve).getName());
    });
  }

  public void testTemplate20TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("template.html", "package.json", "template.ts");
      PsiElement resolve = resolveReference("*myHover<caret>List");
      assertEquals("template.ts", resolve.getContainingFile().getName());
      assertUnresolvedReference("myHover<caret>List");
    });
  }

  public void testNoTemplate20TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("noTemplate.html", "package.json", "noTemplate.ts");
      PsiElement resolve = resolveReference("myHover<caret>List");
      assertEquals("noTemplate.ts", resolve.getContainingFile().getName());
      assertUnresolvedReference("*myHover<caret>List");
    });
  }

  public void testTemplate20JavaScript() {
    configureWithMetadataFiles(myFixture, "template");
    myFixture.configureByFiles("template.html");
    PsiElement resolve = resolveReference("*myHover<caret>List");
    assertEquals("template.metadata.json", resolve.getContainingFile().getName());
    assertUnresolvedReference("myHover<caret>List");
  }

  public void testNoTemplate20JavaScript() {
    configureWithMetadataFiles(myFixture, "noTemplate");
    myFixture.configureByFiles("noTemplate.html");
    PsiElement resolve = resolveReference("myHover<caret>List");
    assertEquals("noTemplate.metadata.json", resolve.getContainingFile().getName());
    assertUnresolvedReference("*myHover<caret>List");
  }

  public void testBindingNamespace() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("bindingNamespace.html", "package.json");
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.enableInspections(XmlUnboundNsPrefixInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testEventNamespace() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("eventNamespace.html", "package.json");
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.enableInspections(XmlUnboundNsPrefixInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testCssExternalReference20() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("cssExtRef.ts", "package.json", "css.css");
      PsiElement resolve = resolveReference("inDa<caret>Class");
      assertEquals("css.css", resolve.getContainingFile().getName());
    });
  }

  public void testCssInternalReference20() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("cssIntRef.ts", "package.json");
      resolveReference("inDa<caret>Class");
    });
  }

  public void testCssInternalReferenceWithHtmlTag20() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("cssIntRefHtmlTag.ts", "package.json");
      resolveReference("inDa<caret>Class");
    });
  }

  public void testCaseCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("case.html", "ng_for_of.ts", "package.json");
      myFixture.completeBasic();
      myFixture.type('\n');
      myFixture.checkResultByFile("case.after.html");
    });
  }

  public void testRouterLink() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "routerLink");
      myFixture.configureByFiles("routerLink.html");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "[routerLink]", "routerLink2");
    });
  }

  public void testComplexSelectorList() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "button");
      myFixture.configureByFiles("material.html");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "mat-icon-button");
    });
  }

  public void testSelectorConcatenationList() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "button");
      myFixture.configureByFiles("material.html");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "mat-raised-button");
    });
  }

  public void testComplexSelectorList2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "ionic");
      myFixture.configureByFiles("ionic.html");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "ion-item");
    });
  }

  public void testVirtualInOuts() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "ionic");
      myFixture.configureByFiles("ionic.html");
      myFixture.type("ion-item ");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "fakeInput", "[fakeInput]", "(fakeOutput)");
    });
  }

  public void testSelectorListSpaces() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("spaces.html", "package.json", "spaces.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "other-attr");
    });
  }

  public void testSelectorListSpaces2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("spaces.html", "package.json", "spaces.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "other-attr");
    });
  }

  public void testSelectorListSpacesCompiled() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      configureWithMetadataFiles(myFixture, "flexOrder");
      myFixture.configureByFiles("flexOrder.html");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "[fxFlexOrder]");
    });
  }

  public void testId() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.enableInspections(XmlInvalidIdInspection.class);
      myFixture.configureByFiles("id.html", "package.json", "object.ts");
      myFixture.checkHighlighting();
    });
  }

  public void testViewChildReferenceNavigation() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      PsiReference reference = myFixture.getReferenceAtCaretPosition("viewChildReference.ts", "package.json");
      assertNotNull(reference);
      PsiElement el = reference.resolve();
      assertNotNull(el);
      assertEquals("#area", el.getParent().getParent().getText());
    });
  }

  public void testViewChildReferenceCodeCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () ->
      assertEquals(asList("area", "area2", "area3"),
                   myFixture.getCompletionVariants("viewChildReference.ts", "package.json"))
    );
  }

  public void testViewChildReferenceNavigationHTML() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      PsiReference reference =
        myFixture.getReferenceAtCaretPosition("viewChildReferenceHTML.ts", "viewChildReferenceHTML.html", "package.json");
      assertNotNull(reference);
      PsiElement el = reference.resolve();
      assertNotNull(el);
      assertEquals("viewChildReferenceHTML.html", el.getContainingFile().getName());
      assertEquals("#area", el.getParent().getParent().getText());
    });
  }

  public void testViewChildReferenceCodeCompletionHTML() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () ->
      assertEquals(asList("area", "area2"),
                   myFixture.getCompletionVariants("viewChildReferenceHTML.ts", "viewChildReferenceHTML.html", "package.json"))
    );
  }

  public void testI18NAttr() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.configureByFiles("i18n.html", "package.json");
      myFixture.checkHighlighting(true, false, true);
    });
  }

  public void testNgNoValidateReference() {
    myFixture.configureByFiles("ngNoValidate.html", "ng_no_validate_directive.ts", "package.json");
    PsiElement resolve = resolveReference("ng<caret>NativeValidate");
    assertInstanceOf(resolve, Angular2DirectiveSelectorPsiElement.class);
    assertEquals("ng_no_validate_directive.ts", resolve.getContainingFile().getName());
  }

  public void testSelectorBasedAttributesCompletion() {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.configureByFiles("selectorBasedAttributes.ts", "package.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(),
                             "[myInput]",
                             "(myOutput)",
                             "[mySimpleBindingInput]", "mySimpleBindingInput",
                             "myPlain",
                             "[myInOut]", "[(myInOut)]",
                             "fake", "[fake]", "[(fake)]",
                             "(fakeChange)");
      assertDoesntContain(myFixture.getLookupElementStrings(),
                          "myInput", "(myInput)", "[(myInput)]",
                          "myOutput", "[myOutput]", "[(myOutput)]",
                          "(mySimpleBindingInput)", "[(mySimpleBindingInput)]",
                          "[myPlain]", "(myPlain)", "[(myPlain)]",
                          "(myInOut)", "myInOut",
                          "myInOutChange", "(myInOutChange)", "[myInOutChange]", "[(myInOutChange)]",
                          "(fake)",
                          "fakeChange, [fakeChange], [(fakeChange)]");
    });
  }

  public void testSelectorBasedAttributesNavigation() {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.configureByFiles("selectorBasedAttributes.ts", "package.json");

      final List<Pair<String, String>> attrWrap = ContainerUtil.newArrayList(
        pair("", ""),
        pair("[", "]"),
        pair("(", ")"),
        pair("[(", ")]")
      );

      for (Map.Entry<String, String> attr : ContainerUtil.<String, String>immutableMapBuilder()
        // <simple><input><output><inout>
        // x -> no resolve, p -> resolve to property, s -> resolve to selector
        .put("myInput", "xpxx")
        .put("mySimpleBindingInput", "ppxx")
        .put("myPlain", "sxxx")
        .put("myOutput", "xxpx")
        .put("myInOut", "xpxp")
        .put("myInOutChange", "xxpx")
        .put("fake", "ccxc")
        .put("fakeChange", "xxcx")
        .build().entrySet()) {

        String name = attr.getKey();
        String checks = attr.getValue();
        for (int i = 0; i < attrWrap.size(); i++) {
          Pair<String, String> wrap = attrWrap.get(i);
          int offsetBySignature = AngularTestUtil.findOffsetBySignature(
            wrap.first + "<caret>" + name + wrap.second + "=", myFixture.getFile());
          PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
          String messageStart = "Attribute " + wrap.first + name + wrap.second;
          switch (checks.charAt(i)) {
            case 'x':
              if (ref != null) {
                assertNull(messageStart + " should not resolve", ref.resolve());
              }
              break;
            case 'p':
              assertNotNull(messageStart + " should have reference", ref);
              assert ref.resolve() instanceof TypeScriptField :
                messageStart + " should resolve to TypeScriptField instead of " + ref.resolve();
              break;
            case 's':
              assertNotNull(messageStart + " should have reference", ref);
              assert ref.resolve() instanceof Angular2DirectiveSelectorPsiElement :
                messageStart + " should resolve to Angular2DirectiveSelectorElement instead of " + ref.resolve();
              break;
            case 'c':
              assertNotNull(messageStart + " should have reference", ref);
              assert ref.resolve() instanceof TypeScriptClass :
                messageStart + " should resolve to Angular2DirectiveSelectorElement instead of " + ref.resolve();
              break;
            default:
              throw new IllegalStateException("wrong char: " + checks.charAt(i));
          }
        }
      }
    });
  }

  public void testExportAs() {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.enableInspections(JSUnresolvedVariableInspection.class);
      myFixture.enableInspections(JSCheckFunctionSignaturesInspection.class);
      myFixture.configureByFiles("exportAs.ts", "package.json");
      myFixture.checkHighlighting(true, false, true);
    });
  }

  public void testMultipleExportAsNames() {
    Registry.get("ide.completion.variant.limit").setValue(10000, getTestRootDisposable());
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.configureByFiles("exportAsMultipleNames.ts", "package.json");
      for (String name : asList("r", "f", "g")) {
        AngularTestUtil.moveToOffsetBySignature("{{ " + name + ".<caret> }}", myFixture);
        myFixture.completeBasic();
        if (name.equals("g")) {
          assertContainsElements(myFixture.getLookupElementStrings(), "length", "type");
        }
        else {
          assertContainsElements(myFixture.getLookupElementStrings(), "foo");
          assertDoesntContain(myFixture.getLookupElementStrings(), "split", "length", "type");
        }
      }
    });
  }

  public void testNgClassCodeCompletion() {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.configureByFiles("ngClass.html", "ngClass.css", "package.json");
      for (String prefix : asList("{", "[", "")) {
        AngularTestUtil.moveToOffsetBySignature("=\"" + prefix + "'foo1 b<caret>'", myFixture);
        myFixture.completeBasic();
        assertEquals(ContainerUtil.set("bar", "boo"), new HashSet<>(myFixture.getLookupElementStrings()));
      }
    });
  }

  public void testNgClassReferences() {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.configureByFiles("ngClass.html", "ngClass.css", "package.json");
      for (String prefix : asList("{", "[", "")) {
        AngularTestUtil.resolveReference("=\"" + prefix + "'fo<caret>o b", myFixture);
        AngularTestUtil.resolveReference("=\"" + prefix + "'foo b<caret>ar", myFixture);
        AngularTestUtil.assertUnresolvedReference("=\"" + prefix + "'f<caret>oo1 ", myFixture);
        AngularTestUtil.assertUnresolvedReference("=\"" + prefix + "'foo1 b<caret>", myFixture);
      }
    });
  }

  public void testOneTimeBindingOfPrimitives() {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.configureByFiles("one_time_binding.html", "one_time_binding.ts", "package.json");
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.checkHighlighting(true, false, true);
    });
  }

  public void testStandardPropertiesOnComponent() {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.configureByFiles("std_props_on_component.html", "one_time_binding.ts", "package.json");
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.checkHighlighting(true, false, true);
    });
  }

  public void testCaseInsensitiveAttrNames() {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.configureByFiles("case_insensitive.html", "one_time_binding.ts", "package.json");
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.checkHighlighting(true, false, true);
    });
  }

  public void testCodeCompletionWithNotSelector() {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      configureWithMetadataFiles(myFixture, "router");
      myFixture.configureByFiles("contentAssistWithNotSelector.html");
      myFixture.completeBasic();
    });
  }

  public void testNoLifecycleHooksInCodeCompletion() {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.configureByFiles("lifecycleHooks.ts", "package.json");
      myFixture.completeBasic();
      assertEquals(ContainerUtil.sorted(myFixture.getLookupElementStrings()),
                   ContainerUtil.newArrayList("$any", "testOne", "testTwo", "testing"));
    });
  }

  public void testDecoratorInGetter() {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.configureByFiles("decoratorInGetter.ts", "package.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "[age]");
    });
  }

  public void testStandardTagProperties() {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.configureByFiles("standardTagProperties.ts", "package.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(),
                             "[autofocus]", "[readOnly]", "[selectionDirection]", "[innerHTML]",
                             "(auxclick)", "(blur)", "(click)", "(paste)", "(webkitfullscreenchange)");
      assertDoesntContain(myFixture.getLookupElementStrings(),
                          "innerHTML", "[class]");
    });
  }

  public void testNgIfAsCodeCompletion() {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.configureByFiles("ngIfAs.ts", "package.json");
      myFixture.completeBasic();
      myFixture.checkResultByFile("ngIfAs.after.ts");
    });
  }

  public void testNoStandardJSEvents() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.configureByFiles("flexOrder.html", "package.json");
      myFixture.completeBasic();
      assertDoesntContain(myFixture.getLookupElementStrings(), "onclick", "onkeyup");
    });
  }

  public void testCodeCompletionItemsTypes() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.configureByFiles("attributeTypes.ts", "lib.dom.d.ts", "package.json");
      myFixture.completeBasic();
      assertContainsElements(
        ContainerUtil.mapNotNull(myFixture.getLookupElements(), el -> {
          TestLookupElementPresentation presentation = TestLookupElementPresentation.renderReal(el);
          return presentation.getItemText() + "#" + presentation.getTypeText();
        }),
        "plainBoolean#boolean",
        "[plainBoolean]#boolean",
        "simpleStringEnum#MyType",
        "[simpleStringEnum]#MyType",
        "(my-event)#MyEvent",
        "(problematicOutput)#T",
        "(complex-event)#MyEvent|MouseEvent",
        "(click)#MouseEvent",
        "(blur)#FocusEvent",
        "[innerHTML]#string"
      );
    });
  }

  public void testMatchedDirectivesProperties() {
    JSTestUtils.testES6(getProject(), () -> {
      configureWithMetadataFiles(myFixture, "common", "forms");
      myFixture.configureByFiles("attributeTypes.ts", "lib.dom.d.ts");
      myFixture.completeBasic();
      assertContainsElements(
        ContainerUtil.mapNotNull(myFixture.getLookupElements(), el -> {
          double priority = 0;
          if (el instanceof PrioritizedLookupElement) {
            priority = ((PrioritizedLookupElement)el).getPriority();
          }
          TestLookupElementPresentation presentation = TestLookupElementPresentation.renderReal(el);

          return (presentation.isItemTextBold() ? "!" : "") + el.getLookupString() + "#" + (int)priority;
        }),
        "!plainBoolean#100",
        "![plainBoolean]#100",
        "!simpleStringEnum#100",
        "![simpleStringEnum]#100",
        "!(my-event)#100",
        "!(ngModelChange)#100",
        "![fooInput]#100",
        "![disabled]#100",
        "[bar]#50",
        "(click)#50",
        "(blur)#50",
        "[innerHTML]#50",
        "*ngIf#50",
        "[attr.#0"
      );
      assertDoesntContain(myFixture.getLookupElementStrings(), "[ngModel]", "ngModel", "[matchedPlainBoolean]");
    });
  }

  public void testCodeCompletionOneTimeSimpleStringEnum() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.configureByFiles("attributeTypes.ts", "package.json");
      myFixture.completeBasic();
      myFixture.type("simpleS\n");
      myFixture.completeBasic();
      assertSameElements(myFixture.getLookupElementStrings(), "off", "polite", "assertive");
    });
  }

  public void testCodeCompletionOneTimeBoolean() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.configureByFiles("attributeTypes.ts", "package.json");
      myFixture.completeBasic();
      //test no auto-value completion
      myFixture.type("plainB\n=");
      myFixture.completeBasic();
      //test type
      assertSameElements(myFixture.getLookupElementStrings(), "plainBoolean");
    });
  }

  public void testCodeCompletionDefaultJSEventType() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.configureByFiles("attributeTypes.ts", "lib.dom.d.ts", "package.json");
      myFixture.completeBasic();
      myFixture.type("(clic\n$event.");
      myFixture.completeBasic();

      assertContainsElements(myFixture.getLookupElementStrings(),
                             "x", "y", "layerX", "layerY", "altKey", "button",
                             "clientX", "clientY", "isTrusted", "timeStamp");
    });
  }

  public void testAttrCompletions() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.configureByFiles("attrTest.ts", "package.json");
      myFixture.completeBasic();
      myFixture.type("att\n");
      myFixture.type("acc\n");
      PsiElement element = AngularTestUtil.resolveReference("[attr.ac<caret>cesskey]", myFixture);
      assertEquals("common.rnc", element.getContainingFile().getName());
    });
  }
}
