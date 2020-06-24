// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
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
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedVariableInspection;
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.util.XmlInvalidIdInspection;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.entities.*;
import org.angular2.inspections.Angular2TemplateInspectionsProvider;
import org.angular2.inspections.AngularUndefinedBindingInspection;
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable;
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable.Kind;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.containers.ContainerUtil.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.angular2.modules.Angular2TestModule.*;
import static org.angularjs.AngularTestUtil.*;

public class AttributesTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return getBaseTestDataPath(getClass()) + "attributes";
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
    myFixture.enableInspections(RequiredAttributesInspection.class,
                                HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.checkHighlighting();
  }

  public void testEventHandlers2() {
    myFixture.configureByFiles("event.html", "package.json");
    myFixture.enableInspections(RequiredAttributesInspection.class,
                                HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.checkHighlighting();
  }

  public void testEventHandlersStandardCompletion2() {
    myFixture.configureByFiles("event.html", "custom.ts", "package.json");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "(mouseover)");
    moveToOffsetBySignature("<some-tag <caret>>", myFixture);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "(mouseover)");
  }

  public void testBindingStandardCompletion2() {
    myFixture.configureByFiles("bindingHtml.html", "package.json");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "[value]");
  }

  public void testTemplateReferenceDeclarations2() {
    myFixture.configureByFiles("variable.html", "custom.ts", "package.json");
    myFixture.enableInspections(RequiredAttributesInspection.class,
                                HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.checkHighlighting();
  }

  public void testTemplateReferenceCompletion2() {
    myFixture.configureByFiles("binding.html", "package.json");
    myFixture.completeBasic();
    myFixture.checkResultByFile("binding.after.html");
  }

  public void testVariableCompletion2() {
    myFixture.configureByFiles("ngTemplate.html", "package.json");
    myFixture.completeBasic();
    myFixture.checkResultByFile("ngTemplate.after.html");
  }

  public void testTemplateReferenceCompletion2Inline() {
    myFixture.configureByFiles("binding.ts", "package.json");
    myFixture.completeBasic();
    myFixture.checkResultByFile("binding.after.ts");
  }

  public void testTemplateReferenceSmart2() {
    myFixture.configureByFiles("binding.type.html", "package.json");
    final PsiFile file = myFixture.getFile();
    final int offset = findOffsetBySignature("user<caret>name,", file);
    final JSReferenceExpression ref = PsiTreeUtil.getParentOfType(file.findElementAt(offset), JSReferenceExpression.class);
    final JSSimpleTypeProcessor processor = new JSSimpleTypeProcessor();
    JSTypeEvaluator.evaluateTypes(ref, file, processor);
    final JSType type = processor.getType();
    assertInstanceOf(type, JSNamedType.class);
    assertEquals("HTMLInputElement", type.getTypeText());
  }

  public void testTemplateReferenceResolve2() {
    myFixture.configureByFiles("binding.after.html", "package.json");
    PsiElement resolve = resolveReference("$event, user<caret>name");
    assertInstanceOf(resolve, Angular2HtmlAttrVariable.class);
    assertEquals("binding.after.html", resolve.getContainingFile().getName());
    assertEquals("#username", resolve.getParent().getParent().getText());
  }

  public void testVariableResolve2() {
    myFixture.configureByFiles("ngTemplate.after.html", "package.json");
    PsiElement resolve = resolveReference("{{my_<caret>user");
    assertInstanceOf(resolve, Angular2HtmlAttrVariable.class);
    assertEquals(Kind.LET, ((Angular2HtmlAttrVariable)resolve).getKind());
    assertEquals("ngTemplate.after.html", resolve.getContainingFile().getName());
    assertEquals("let-my_user", resolve.getParent().getParent().getText());
  }

  public void testTemplateReferenceResolve2Inline() {
    myFixture.configureByFiles("binding.after.ts", "package.json");
    PsiElement resolve = resolveReference("in<caret>put_el.");
    assertInstanceOf(resolve, Angular2HtmlAttrVariable.class);
    assertEquals(Kind.REFERENCE, ((Angular2HtmlAttrVariable)resolve).getKind());
    assertEquals("binding.after.ts", resolve.getContainingFile().getName());
    assertEquals("#input_el", resolve.getParent().getParent().getText());
  }

  public void testBindingCompletion2TypeScript() {
    myFixture.configureByFiles("object_binding.html", "package.json", "object.ts");
    myFixture.completeBasic();
    myFixture.checkResultByFile("object_binding.after.html");
  }

  public void testBindingResolve2TypeScript() {
    myFixture.configureByFiles("object_binding.after.html", "package.json", "object.ts");
    PsiElement resolve = resolveReference("[mod<caret>el]");
    assertEquals("object.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, JSField.class);
  }

  public void testBindingResolve2TypeScriptInputInDecorator() {
    myFixture.copyFileToProject("object_in_dec.ts");
    myFixture.configureByFiles("object_binding.after.html", "package.json");
    PsiElement resolve = resolveReference("[mod<caret>el]");
    assertEquals("object_in_dec.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, JSField.class);

    TypeScriptClass cls = PsiTreeUtil.getContextOfType(resolve, TypeScriptClass.class);
    assert cls != null;
    Angular2Directive component = Angular2EntitiesProvider.getComponent(cls);
    assert component != null;
    assertEquals(newHashSet("model", "id", "oneTime", "oneTimeList"),
                 component.getInputs().stream().map(Angular2DirectiveProperty::getName).collect(Collectors.toSet()));
    assertEquals(Collections.singleton("complete"),
                 component.getOutputs().stream().map(Angular2DirectiveProperty::getName).collect(Collectors.toSet()));
    assertEquals(newHashSet("testAttrOne", "testAttrTwo", "testAttrThree"),
                 component.getAttributes().stream().map(Angular2DirectiveAttribute::getName).collect(Collectors.toSet()));

    moveToOffsetBySignature("[model]=\"\"<caret>", myFixture);
    myFixture.type(' ');
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "testAttrOne", "testAttrTwo", "testAttrThree");
  }

  public void testStaticAttributes() {
    myFixture.copyFileToProject("object_in_dec.ts");
    myFixture.configureByFiles("static_attributes.html", "package.json");
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.checkHighlighting();
  }

  public void testBindingCompletionViaBase2TypeScript() {
    myFixture.configureByFiles("object_binding_via_base.html", "package.json", "inheritor.ts", "object.ts");
    myFixture.completeBasic();
    myFixture.checkResultByFile("object_binding_via_base.after.html");
  }

  public void testBindingResolveViaBase2TypeScript() {
    myFixture.configureByFiles("object_binding_via_base.after.html", "package.json", "inheritor.ts", "object.ts");
    PsiElement resolve = resolveReference("[mod<caret>el]");
    assertEquals("object.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, JSField.class);
  }

  public void testBindingOverride2CompletionTypeScript() {
    myFixture.configureByFiles("object_binding.html", "package.json", "objectOverride.ts");
    myFixture.completeBasic();
    myFixture.checkResultByFile("object_binding.after.html");
  }

  public void testBindingOverrideResolve2TypeScript() {
    myFixture.configureByFiles("object_binding.after.html", "package.json", "objectOverride.ts");
    PsiElement resolve = resolveReference("[mod<caret>el]");
    assertEquals("objectOverride.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, JSField.class);
  }

  public void testBindingAttributeCompletion2TypeScript() {
    myFixture.configureByFiles("attribute_binding.html", "package.json", "object.ts");
    myFixture.completeBasic();
    myFixture.checkResultByFile("attribute_binding.after.html");
  }

  public void testBindingAttributeResolve2TypeScript() {
    myFixture.configureByFiles("attribute_binding.after.html", "package.json", "object.ts");
    PsiElement resolve = resolveReference("[mod<caret>el]");
    assertEquals("object.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, JSField.class);
  }

  public void testOneTimeBindingAttributeCompletion2TypeScript() {
    myFixture.configureByFiles("attribute_one_time_binding.html", "package.json", "object.ts");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "model", "oneTime", "oneTimeList");
  }

  public void testOneTimeBindingAttributeCompletion3TypeScript() {
    myFixture.configureByFiles("attribute_one_time_binding.html", "package.json", "object.ts");
    myFixture.completeBasic();
    myFixture.type("onetli\n");
    myFixture.checkResultByFile("attribute_one_time_binding.after.html");
  }

  public void testOneTimeBindingAttributeResolve2TypeScript() {
    myFixture.configureByFiles("attribute_one_time_binding.after.html", "package.json", "object.ts");
    PsiElement resolve = resolveReference("one<caret>TimeList");
    assertEquals("object.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, JSField.class);
  }

  public void testOneTimeBindingAttributeResolve2JavaScript() {
    configureLink(myFixture, ANGULAR_MATERIAL_7_2_1);
    myFixture.configureByFiles("compiled_binding.after.html");
    PsiElement resolve = resolveReference("col<caret>or");
    assertEquals("color.d.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, TypeScriptPropertySignature.class);
    assertEquals("/** Theme color palette for the component. */\n" +
                 "    color: ThemePalette", resolve.getText());
  }

  public void testOneTimeBindingAttributeCompletion2JavaScript() {
    configureLink(myFixture, ANGULAR_MATERIAL_7_2_1);
    myFixture.configureByFiles("compiled_binding.html");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "color");
  }

  public void testOneTimeBindingAttributeCompletion2ES6() {
    configureLink(myFixture, ANGULAR_MATERIAL_7_2_1);
    myFixture.configureByFiles("compiled_binding.html");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "color");
    assertDoesntContain(myFixture.getLookupElementStrings(), "tabIndex");
  }

  public void testBindingAttributeFunctionCompletion2TypeScript() {
    myFixture.configureByFiles("attribute_binding.html", "package.json", "object_with_function.ts");
    myFixture.completeBasic();
    myFixture.checkResultByFile("attribute_binding.after.html");
  }

  public void testBindingAttributeFunctionResolve2TypeScript() {
    myFixture.configureByFiles("attribute_binding.after.html", "package.json", "object_with_function.ts");
    PsiElement resolve = resolveReference("[mod<caret>el]");
    assertEquals("object_with_function.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, JSFunction.class);
  }

  public void testEventHandlerCompletion2TypeScript() {
    myFixture.configureByFiles("object_event.html", "package.json", "object.ts");
    myFixture.completeBasic();
    myFixture.checkResultByFile("object_event.after.html");
  }

  public void testEventHandlerResolve2TypeScript() {
    myFixture.configureByFiles("object_event.after.html", "package.json", "object.ts");
    PsiElement resolve = resolveReference("(co<caret>mplete)");
    assertEquals("object.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, JSField.class);
  }

  public void testEventHandlerOverrideCompletion2TypeScript() {
    myFixture.configureByFiles("object_event.html", "package.json", "objectOverride.ts");
    myFixture.completeBasic();
    myFixture.checkResultByFile("object_event.after.html");
  }

  public void testEventHandlerOverrideResolve2TypeScript() {
    myFixture.configureByFiles("object_event.after.html", "package.json", "objectOverride.ts");
    PsiElement resolve = resolveReference("(co<caret>mplete)");
    assertEquals("objectOverride.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, JSField.class);
  }

  public void testForCompletion2TypeScript() {
    myFixture.configureByFiles("for2.html", "ng_for_of.ts", "package.json");
    int offsetBySignature = findOffsetBySignature("ngF<caret>", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "ngFor", "[ngForOf]");
  }

  public void testForOfResolve2Typescript() {
    myFixture.configureByFiles("for2.html", "ng_for_of.ts", "package.json");
    PsiElement resolve = resolveReference("ngF<caret>");
    assertEquals("ng_for_of.ts", resolve.getContainingFile().getName());
    assertEquals("ngFor", resolve.getText());
    assertEquals("@Directive({selector: '[ngFor][ngForOf]'})", getDirectiveDefinitionText(resolve));
  }

  public void testForCompletion2Javascript() {
    configureLink(myFixture, ANGULAR_COMMON_4_0_0);
    myFixture.configureByFiles("for2.html");
    int offsetBySignature = findOffsetBySignature("ngF<caret>", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "ngFor", "[ngForOf]");
  }

  public void testIfCompletion4JavascriptUmd() {
    configureLink(myFixture, ANGULAR_COMMON_4_0_0);
    myFixture.configureByFiles("if4.html");
    int offsetBySignature = findOffsetBySignature("*<caret>", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "*ngIf");
    assertDoesntContain(myFixture.getLookupElementStrings(), "ngIf");
  }

  public void testForTemplateCompletion2Javascript() {
    configureLink(myFixture, ANGULAR_COMMON_4_0_0);
    myFixture.configureByFiles("for2Template.html");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "*ngFor");
  }

  public void testForOfResolve2Javascript() {
    configureLink(myFixture, ANGULAR_COMMON_4_0_0);
    myFixture.configureByFiles("for2.html");
    PsiElement resolve = resolveReference("ngF<caret>");
    assertEquals("ng_for_of.d.ts", resolve.getContainingFile().getName());
  }

  public void testTemplateUrl20Completion() {
    myFixture.configureByFiles("custom.ts", "package.json", "custom.html");
    int offsetBySignature = findOffsetBySignature("templateUrl: '<caret>", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "custom.ts", "package.json", "custom.html");
  }

  public void testTemplateUrl20Resolve() {
    myFixture.configureByFiles("custom.template.ts", "package.json", "custom.html");
    PsiElement resolve = resolveReference("templateUrl: '<caret>");
    assertInstanceOf(resolve, PsiFile.class);
    assertEquals("custom.html", ((PsiFile)resolve).getName());
  }

  public void testStyleUrls20Completion() {
    myFixture.configureByFiles("custom.ts", "package.json", "custom.html");
    int offsetBySignature = findOffsetBySignature("styleUrls: ['<caret>", myFixture.getFile());
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "custom.ts", "package.json", "custom.html");
  }

  public void testStyleUrls20Resolve() {
    myFixture.configureByFiles("custom.style.ts", "package.json", "custom.html");
    PsiElement resolve = resolveReference("styleUrls: ['<caret>");
    assertInstanceOf(resolve, PsiFile.class);
    assertEquals("custom.html", ((PsiFile)resolve).getName());
  }

  public void testBindingNamespace() {
    myFixture.configureByFiles("bindingNamespace.html", "package.json");
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class,
                                XmlUnboundNsPrefixInspection.class);
    myFixture.checkHighlighting();
  }

  public void testEventNamespace() {
    myFixture.configureByFiles("eventNamespace.html", "package.json");
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class,
                                XmlUnboundNsPrefixInspection.class);
    myFixture.checkHighlighting();
  }

  public void testCaseCompletion2() {
    myFixture.configureByFiles("case.html", "ng_for_of.ts", "package.json");
    myFixture.completeBasic();
    myFixture.checkResultByFile("case.after.html");
  }

  public void testTemplatesCompletion() {
    configureLink(myFixture, ANGULAR_COMMON_4_0_0);
    myFixture.configureByFiles("templates_completion.html");
    myFixture.completeBasic();
    assertEquals(asList("*ngIf", "*ngSwitchCase", "*ngSwitchDefault"),
                 sorted(myFixture.getLookupElementStrings()));
  }

  public void testTemplatesCompletion2() {
    configureLink(myFixture, ANGULAR_COMMON_4_0_0);
    myFixture.configureByFiles("templates_completion2.html");
    myFixture.completeBasic();
    assertEquals(asList("*ngComponentOutlet", "*ngPluralCase", "*ngSwitchCase", "[ngClass]", "[ngComponentOutlet]", "ngComponentOutlet"),
                 sorted(myFixture.getLookupElementStrings()));
  }

  public void testMaterialSelectors() {
    configureLink(myFixture, ANGULAR_MATERIAL_7_2_1);
    myFixture.configureByFiles("material.html");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "mat-icon-button", "mat-raised-button");
  }

  public void testComplexSelectorList2() {
    configureLink(myFixture, IONIC_ANGULAR_3_0_1);
    myFixture.configureByFiles("div.html");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "ion-item");
  }

  public void testSelectorListSpaces() {
    myFixture.configureByFiles("spaces.html", "package.json", "spaces.ts");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "other-attr");
  }

  public void testSelectorListSpaces2() {
    myFixture.configureByFiles("spaces.html", "package.json", "spaces.ts");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "other-attr");
  }

  public void testId() {
    myFixture.enableInspections(XmlInvalidIdInspection.class);
    myFixture.configureByFiles("id.html", "package.json", "object.ts");
    myFixture.checkHighlighting();
  }

  public void testViewChildReferenceNavigation() {
    PsiReference reference = myFixture.getReferenceAtCaretPosition("viewChildReference.ts", "package.json");
    assertNotNull(reference);
    PsiElement el = reference.resolve();
    assertNotNull(el);
    assertEquals("#area", el.getParent().getParent().getText());
  }

  public void testViewChildrenReferenceNavigation() {
    PsiReference reference = myFixture.getReferenceAtCaretPosition("viewChildrenReference.ts", "package.json");
    assertNotNull(reference);
    PsiElement el = reference.resolve();
    assertNotNull(el);
    assertEquals("#area", el.getParent().getParent().getText());
  }

  public void testViewChildReferenceCodeCompletion() {
    assertEquals(asList("area", "area2", "area3"),
                 myFixture.getCompletionVariants("viewChildReference.ts", "package.json"));
  }

  public void testViewChildReferenceNavigationHTML() {
    PsiReference reference =
      myFixture.getReferenceAtCaretPosition("viewChildReferenceHTML.ts", "viewChildReferenceHTML.html", "package.json");
    assertNotNull(reference);
    PsiElement el = reference.resolve();
    assertNotNull(el);
    assertEquals("viewChildReferenceHTML.html", el.getContainingFile().getName());
    assertEquals("#area", el.getParent().getParent().getText());
  }

  public void testViewChildReferenceCodeCompletionHTML() {
    assertEquals(asList("area", "area2"),
                 myFixture.getCompletionVariants("viewChildReferenceHTML.ts", "viewChildReferenceHTML.html", "package.json"));
  }

  public void testViewChildrenReferenceCodeCompletionHTML() {
    assertEquals(asList("area2", "area3"),
                 myFixture.getCompletionVariants("viewChildrenReferenceHTML.ts", "viewChildrenReferenceHTML.html", "package.json"));
  }

  public void testI18NAttr() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.configureByFiles("i18n.html", "package.json");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testNgNoValidateReference() {
    myFixture.configureByFiles("ngNoValidate.html", "ng_no_validate_directive.ts", "package.json");
    PsiElement resolve = resolveReference("ng<caret>NativeValidate");
    assertInstanceOf(resolve, Angular2DirectiveSelectorPsiElement.class);
    assertEquals("ng_no_validate_directive.ts", resolve.getContainingFile().getName());
  }

  public void testSelectorBasedAttributesCompletion() {
    myFixture.configureByFiles("selectorBasedAttributes.ts", "package.json");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "myInput", "[myInput]",
                           "(myOutput)",
                           "[mySimpleBindingInput]", "mySimpleBindingInput",
                           "myPlain",
                           "myInOut", "[myInOut]", "[(myInOut)]",
                           "fake", "[fake]", "[(fake)]",
                           "(fakeChange)");
    assertDoesntContain(myFixture.getLookupElementStrings(),
                        "(myInput)", "[(myInput)]",
                        "myOutput", "[myOutput]", "[(myOutput)]",
                        "(mySimpleBindingInput)", "[(mySimpleBindingInput)]",
                        "[myPlain]", "(myPlain)", "[(myPlain)]",
                        "(myInOut)",
                        "myInOutChange", "(myInOutChange)", "[myInOutChange]", "[(myInOutChange)]",
                        "(fake)",
                        "fakeChange, [fakeChange], [(fakeChange)]");
  }

  public void testSelectorBasedAttributesNavigation() {
    myFixture.configureByFiles("selectorBasedAttributes.ts", "package.json");

    final List<Pair<String, String>> attrWrap = newArrayList(
      pair("", ""),
      pair("[", "]"),
      pair("(", ")"),
      pair("[(", ")]")
    );

    for (Map.Entry<String, String> attr : ContainerUtil.<String, String>immutableMapBuilder()
      // <simple><input><output><inout>
      // x -> no resolve, p -> resolve to property, s -> resolve to selector
      .put("myInput", "ppxx")
      .put("mySimpleBindingInput", "ppxx")
      .put("myPlain", "sxxx")
      .put("myOutput", "xxpx")
      .put("myInOut", "ppxp")
      .put("myInOutChange", "xxpx")
      .put("fake", "ccxc")
      .put("fakeChange", "xxcx")
      .build().entrySet()) {

      String name = attr.getKey();
      String checks = attr.getValue();
      for (int i = 0; i < attrWrap.size(); i++) {
        Pair<String, String> wrap = attrWrap.get(i);
        int offsetBySignature = findOffsetBySignature(
          wrap.first + "<caret>" + name + wrap.second + "=", myFixture.getFile());
        PsiPolyVariantReference ref = (PsiPolyVariantReference)myFixture.getFile().findReferenceAt(offsetBySignature);
        String messageStart = "Attribute " + wrap.first + name + wrap.second;
        switch (checks.charAt(i)) {
          case 'x':
            if (ref != null) {
              assertEquals(messageStart + " should not resolve",
                           Collections.emptyList(), asList(ref.multiResolve(false)));
            }
            break;
          case 'p':
            assertNotNull(messageStart + " should have reference", ref);
            assert all(multiResolve(ref), TypeScriptField.class::isInstance) :
              messageStart + " should resolve to TypeScriptField instead of " +
              multiResolve(ref);
            break;
          case 's':
            assertNotNull(messageStart + " should have reference", ref);
            assert all(multiResolve(ref), Angular2DirectiveSelectorPsiElement.class::isInstance) :
              messageStart + " should resolve to Angular2DirectiveSelectorElement instead of " +
              multiResolve(ref);
            break;
          case 'c':
            assertNotNull(messageStart + " should have reference", ref);
            assert all(multiResolve(ref), TypeScriptClass.class::isInstance) :
              messageStart + " should resolve to Angular2DirectiveSelectorElement instead of " +
              multiResolve(ref);
            break;
          default:
            throw new IllegalStateException("wrong char: " + checks.charAt(i));
        }
      }
    }
  }

  @NotNull
  private static List<PsiElement> multiResolve(@NotNull PsiPolyVariantReference ref) {
    return mapNotNull(ref.multiResolve(false),
                      result -> result.isValidResult() ? result.getElement() : null);
  }

  public void testExportAs() {
    myFixture.enableInspections(TypeScriptUnresolvedVariableInspection.class,
                                TypeScriptValidateTypesInspection.class);
    myFixture.configureByFiles("exportAs.ts", "package.json");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testMultipleExportAsNames() {
    Registry.get("ide.completion.variant.limit").setValue(10000, getTestRootDisposable());
    myFixture.configureByFiles("exportAsMultipleNames.ts", "package.json");
    for (String name : asList("r", "f", "g")) {
      moveToOffsetBySignature("{{ " + name + ".<caret> }}", myFixture);
      myFixture.completeBasic();
      if (name.equals("g")) {
        assertContainsElements(myFixture.getLookupElementStrings(), "length", "type");
      }
      else {
        assertContainsElements(myFixture.getLookupElementStrings(), "foo");
        assertDoesntContain(myFixture.getLookupElementStrings(), "split", "length", "type");
      }
    }
  }

  public void testOneTimeBindingOfPrimitives() {
    myFixture.configureByFiles("one_time_binding.html", "one_time_binding.ts", "package.json");
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.checkHighlighting(true, false, true);
  }

  public void testStandardPropertiesOnComponent() {
    myFixture.configureByFiles("std_props_on_component.html", "one_time_binding.ts", "package.json");
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.checkHighlighting(true, false, true);
  }

  public void testCaseInsensitiveAttrNames() {
    myFixture.configureByFiles("case_insensitive.html", "one_time_binding.ts", "package.json");
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.checkHighlighting(true, false, true);
  }

  public void testCodeCompletionWithNotSelector() {
    configureLink(myFixture, ANGULAR_ROUTER_4_0_0);
    myFixture.configureByFiles("contentAssistWithNotSelector.html");
    myFixture.completeBasic();
  }

  public void testNoLifecycleHooksInCodeCompletion() {
    myFixture.configureByFiles("lifecycleHooks.ts", "package.json");
    myFixture.completeBasic();
    assertEquals(sorted(myFixture.getLookupElementStrings()),
                 newArrayList("$any", "testOne", "testTwo", "testing"));
  }

  public void testDecoratorInGetter() {
    myFixture.configureByFiles("decoratorInGetter.ts", "package.json");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "[age]");
  }

  public void testStandardTagProperties() {
    myFixture.configureByFiles("standardTagProperties.ts", "package.json");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "[autofocus]", "[readOnly]", "[selectionDirection]", "[innerHTML]",
                           "(auxclick)", "(blur)", "(click)", "(paste)", "(webkitfullscreenchange)");
    assertDoesntContain(myFixture.getLookupElementStrings(),
                        "innerHTML", "[class]");
  }

  public void testNgIfAsCodeCompletion() {
    myFixture.configureByFiles("ngIfAs.ts", "package.json");
    myFixture.completeBasic();
    myFixture.checkResultByFile("ngIfAs.after.ts");
  }

  public void testCodeCompletionItemsTypes() {
    myFixture.configureByFiles("attributeTypes.ts", "lib.dom.d.ts", "package.json");
    myFixture.completeBasic();
    assertContainsElements(
      renderLookupItems(myFixture, false, true),
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
  }

  public void testMatchedDirectivesProperties() {
    configureLink(myFixture, ANGULAR_COMMON_4_0_0, ANGULAR_FORMS_4_0_0);
    myFixture.configureByFiles("attributeTypes.ts", "lib.dom.d.ts");
    myFixture.completeBasic();
    assertContainsElements(
      renderLookupItems(myFixture, true, false),
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
  }

  public void testCodeCompletionOneTimeSimpleStringEnum() {
    myFixture.configureByFiles("attributeTypes.ts", "package.json");
    myFixture.completeBasic();
    myFixture.type("simpleS\n");
    myFixture.completeBasic();
    assertSameElements(myFixture.getLookupElementStrings(), "off", "polite", "assertive");
  }

  public void testCodeCompletionOneTimeSimpleStringEnumSetter() {
    myFixture.configureByFiles("attributeTypes.ts", "package.json");
    myFixture.completeBasic();
    myFixture.type("setterSi\n");
    myFixture.completeBasic();
    assertSameElements(myFixture.getLookupElementStrings(), "off", "polite", "assertive");
  }

  public void testCodeCompletionOneTimeBoolean() {
    myFixture.configureByFiles("attributeTypes.ts", "package.json");
    myFixture.completeBasic();
    //test no auto-value completion
    myFixture.type("plainB\n=");
    myFixture.completeBasic();
    //test type
    assertSameElements(myFixture.getLookupElementStrings(), "plainBoolean");
  }

  public void testCodeCompletionDefaultJSEventType() {
    myFixture.configureByFiles("attributeTypes.ts", "lib.dom.d.ts", "package.json");
    myFixture.completeBasic();
    myFixture.type("(clic\n$event.");
    myFixture.completeBasic();

    assertContainsElements(myFixture.getLookupElementStrings(),
                           "x", "y", "layerX", "layerY", "altKey", "button",
                           "clientX", "clientY", "isTrusted", "timeStamp");
  }

  public void testAttrCompletions() {
    myFixture.configureByFiles("attrTest.ts", "package.json");
    myFixture.completeBasic();
    myFixture.type("att\n");
    myFixture.type("acc\n");
    PsiElement element = AngularTestUtil.resolveReference("[attr.ac<caret>cesskey]=\"\"", myFixture);
    assertEquals("common.rnc", element.getContainingFile().getName());
  }

  public void testAttrCompletionsCustomTag() {
    myFixture.configureByFiles("attrTestCustom.ts", "package.json");
    myFixture.completeBasic();
    myFixture.type("[attr.");
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "about]", "accesskey]",
                           "id]", "style]", "title]",
                           "aria-atomic]");
    assertDoesntContain(myFixture.getLookupElementStrings(),
                        "innerHTML]");
    myFixture.type("acc\n");
    PsiElement element = AngularTestUtil.resolveReference("[attr.ac<caret>cesskey]=\"\"", myFixture);
    assertEquals("common.rnc", element.getContainingFile().getName());
  }

  public void testAttrCompletions2() {
    myFixture.configureByFiles("div.html", "package.json");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "[attr.");
    myFixture.type("att\n");
    assertEquals("[attr.]", myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getText());
    myFixture.type("aat\n");
    PsiElement element = AngularTestUtil.resolveReference("[attr.aria-atomic<caret>]=\"\"", myFixture);
    assertEquals("aria.rnc", element.getContainingFile().getName());
  }

  public void testCanonicalCompletion() {
    myFixture.configureByFiles("div.html", "package.json");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "bind-", "bindon-", "on-", "ref-");
    myFixture.type("bind-");
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "tabIndex", "innerHTML");
    myFixture.type("iH\n");
    PsiElement element = AngularTestUtil.resolveReference("bind-inner<caret>HTML", myFixture);
    assertEquals("lib.dom.d.ts", element.getContainingFile().getName());
  }

  public void testAttrCompletionsCanonical() {
    myFixture.configureByFiles("attrTest.ts", "package.json");
    myFixture.completeBasic();
    myFixture.type("bind-");
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "attr.");
    myFixture.type("at\naat\n");
    PsiElement element = AngularTestUtil.resolveReference("bind-attr.aria-atomic<caret>=\"\"", myFixture);
    assertEquals("aria.rnc", element.getContainingFile().getName());
  }

  public void testExtKeyEvent() {
    myFixture.configureByFiles("attrTest.ts", "package.json");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "(keyup.", "(keydown.");
    myFixture.type("keyd.\n");
    assertEquals("(keydown.)", myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getText());
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "meta.", "control.", "shift.", "alt.", "escape)", "home)", "f11)");
    myFixture.type("alt.");
    assertEquals("(keydown.alt.)", myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getText());
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "meta.", "control.", "escape)", "home)", "f11)");
    assertDoesntContain(myFixture.getLookupElementStrings(),
                        "alt.");
    myFixture.type("ins\n");
    PsiElement element = AngularTestUtil.resolveReference("(keydown.alt.<caret>insert)=\"\"", myFixture);
    assertEquals("core-scripting.rnc", element.getContainingFile().getName());
  }

  public void testExtKeyEventCanonical() {
    myFixture.configureByFiles("attrTest.ts", "package.json");
    myFixture.completeBasic();
    myFixture.type("on-");
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "keyup.", "keydown.");
    myFixture.type("keyu.\n");
    assertEquals("on-keyup.", myFixture.getFile().findElementAt(myFixture.getCaretOffset() - 1).getText());
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "meta.", "control.", "shift.", "alt.", "escape", "home");
    myFixture.type("alt.");
    assertEquals("on-keyup.alt.", myFixture.getFile().findElementAt(myFixture.getCaretOffset() - 1).getText());
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "meta.", "control.", "escape", "home");
    assertDoesntContain(myFixture.getLookupElementStrings(),
                        "alt.");
    myFixture.type("m.\n");
    assertEquals("on-keyup.alt.meta.", myFixture.getFile().findElementAt(myFixture.getCaretOffset() - 1).getText());
    assertDoesntContain(myFixture.getLookupElementStrings(),
                        "alt.", "meta.");
    myFixture.type("esc\n");
    PsiElement element = AngularTestUtil.resolveReference("on-keyup.alt.meta.<caret>escape=\"\"", myFixture);
    assertEquals("core-scripting.rnc", element.getContainingFile().getName());
  }

  public void testExtKeyEventsInspections() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.configureByFiles("extKeyEvents.html", "custom.ts", "package.json");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testAttributeNameMapping() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.configureByFiles("attributeNameMapping.html", "package.json");
    myFixture.checkHighlighting();
  }

  public void testMultiSelectorTemplateBindings() {
    myFixture.configureByFiles("multi-selector-template-bindings.html", "multi-selector-template-bindings.ts", "package.json");
    myFixture.completeBasic();
    assertEquals(asList("*appIf", "*appUnless"),
                 sorted(myFixture.getLookupElementStrings()));
    assertEquals("multi-selector-template-bindings.ts",
                 resolveReference("*app<caret>If=").getContainingFile().getName());
    assertEquals("multi-selector-template-bindings.ts",
                 resolveReference("*app<caret>Unless=").getContainingFile().getName());
    assertUnresolvedReference("*app<caret>Foo=");
  }

  public void testTypeAttrWithFormsCompletion() {
    configureLink(myFixture, ANGULAR_COMMON_4_0_0, ANGULAR_FORMS_4_0_0);
    myFixture.configureByFiles("typeAttrWithForms.html", "typeAttrWithForms.ts");
    moveToOffsetBySignature("<button <caret>>", myFixture);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "type");

    moveToOffsetBySignature("<input <caret>/>", myFixture);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "type");
  }

  public void testMultiResolve() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureByFiles("multi-resolve.html", "multi-resolve.ts", "package.json");
    myFixture.checkHighlighting();

    Map<String, List<String>> data = newLinkedHashMap(
      pair("id=", asList("id", "id: /*c1*/ string")),
      pair("[id]=", asList("/**\n" +
                           "     * Returns the value of element's id content attribute. Can be set to change it.\n" +
                           "     */\n" +
                           "    id: string", "id: /*c1*/ string")),
      pair("[attr.id]=", singletonList("id")),
      pair("bar=", asList("bar: /*c1*/ number", "bar: /*c2*/ number")),
      pair("[bar]=", asList("bar: /*c1*/ number", "bar: /*c2*/ number")),
      pair("boo=", asList("boo: /*c1*/ number", "boo: /*c2*/ string")),
      pair("[boo]=", asList("boo: /*c1*/ number", "boo: /*c2*/ string"))
    );

    for (Map.Entry<String, List<String>> entry : data.entrySet()) {
      String location = entry.getKey();
      assertSameElements(multiResolveReference(location.charAt(0) + "<caret>" + location.substring(1), myFixture)
                           .stream()
                           .map(el -> {
                             if (el.getText() != null) {
                               return el.getText();
                             }
                             else {
                               return el.getParent().getText();
                             }
                           })
                           .sorted()
                           .collect(Collectors.toList()),
                         sorted(entry.getValue()));
    }
  }

  public void testNgTemplateOutletCompletion() {
    myFixture.configureByFiles("ng-template-outlet-test.html", "ng_template_outlet.ts", "ng_if.ts", "package.json");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), asList("*ngIf", "*ngTemplateOutlet"));
  }

  public void testNgContentCompletion() {
    myFixture.configureByFiles("ng-content-completion.html", "package.json");
    myFixture.completeBasic();
    assertEquals(singletonList("select"), myFixture.getLookupElementStrings());
  }

  public void testNgContentInspection() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.configureByFiles("ng-content-inspection.html", "package.json");
    myFixture.checkHighlighting();
  }

  public void testDirectiveAttributesCompletion() {
    myFixture.configureByFiles("directive_attrs_completion.ts", "package.json");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "foo");
    assertDoesntContain(myFixture.getLookupElementStrings(), "bar", "[bar]", "[foo]", "test", "[test]");
    myFixture.type("foo=\" ");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "bar", "test", "[test]");
    assertDoesntContain(myFixture.getLookupElementStrings(), "[bar]", "foo", "[foo]");
  }

  public void testNgNonBindable() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureByFiles("ngNonBindable.html", "package.json");
    myFixture.checkHighlighting();
  }

  public void testIonicAttributes() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    configureLink(myFixture, IONIC_ANGULAR_4_1_1);
    myFixture.configureByFiles("ionicAttributes.html");
    myFixture.checkHighlighting();
  }

  public void testSvgAttributes() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureByFiles("svg-test.html", "svg-test.ts", "package.json");
    myFixture.checkHighlighting();
    moveToOffsetBySignature("<svg:clipPath id=\"clip\"><caret>", myFixture);
    myFixture.type("<svg:circle [attr.");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "cx]", "cy]", "visibility]", "text-rendering]");
  }

  public void testCustomDataAttributes() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureByFiles("data-attributes.html", "object.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testInputTypeCompletion() {
    configureLink(myFixture, ANGULAR_FORMS_4_0_0);
    myFixture.configureByText("input-type.html", "<input type=\"<caret>\"");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(),
                           "button", "checkbox", "color", "date", "datetime-local", "email", "file", "hidden", "image", "month",
                           "number", "password", "radio", "range", "reset", "search", "submit", "tel", "text", "time", "url", "week");
  }

  public void testI18nCompletion() {
    myFixture.configureByFile("package.json");
    myFixture.configureByText("i18n.html", "<div foo='12' <caret>");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "i18n-");
    myFixture.type("i1-\n");
    assertEquals(singletonList("foo"), myFixture.getLookupElementStrings());
    myFixture.type("\n ");
    myFixture.completeBasic();
    assertDoesntContain(myFixture.getLookupElementStrings(), "i18n-");
    assertContainsElements(myFixture.getLookupElementStrings(), "i18n");
  }

  public void testI18nResolve() {
    myFixture.configureByFile("package.json");
    myFixture.configureByText("i18n.html", "<div foo='12' i18n-f<caret>oo>");
    assertEquals("foo='12'", myFixture.getElementAtCaret().getText());
  }

  public void testHammerJS() {
    myFixture.configureByFile("package.json");
    myFixture.configureByText("hammer.html", "<div <caret>");
    myFixture.completeBasic();
    myFixture.type("(");
    assertContainsElements(renderLookupItems(myFixture, false, true),
                           "(pan)#HammerInput", "(panstart)#HammerInput", "(pinch)#HammerInput", "(tap)#HammerInput");
    myFixture.type("pan\n\" on-");
    myFixture.completeBasic();
    assertDoesntContain(myFixture.getLookupElementStrings(), "pan");
    assertContainsElements(myFixture.getLookupElementStrings(), "panstart", "pinch", "tap");
    myFixture.type("pansta\n");
    myFixture.checkResult("<div (pan)=\"\" on-panstart=\"<caret>\"");
  }
}
