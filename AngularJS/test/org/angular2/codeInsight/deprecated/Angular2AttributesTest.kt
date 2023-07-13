// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.deprecated

import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.lang.javascript.psi.resolve.JSSimpleTypeProcessor
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.lang.javascript.psi.types.JSNamedType
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.containers.ContainerUtil
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbolDelegate.Companion.unwrapAllDelegates
import com.intellij.xml.util.XmlInvalidIdInspection
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureCopy
import org.angular2.Angular2TestModule.Companion.configureLink
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2DirectiveAttribute
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.entities.Angular2DirectiveSelectorSymbol
import org.angular2.entities.Angular2EntitiesProvider.getComponent
import org.angular2.codeInsight.inspections.Angular2ExpressionTypesInspectionTest
import org.angular2.inspections.AngularUndefinedBindingInspection
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable
import org.angular2.web.Angular2WebSymbolsQueryConfigurator
import org.angularjs.AngularTestUtil

@Deprecated("Use test appropriate for IDE feature being tested - e.g. completion/resolve/highlighting ")
class Angular2AttributesTest : Angular2CodeInsightFixtureTestCase() {
  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    // Let's ensure we don't get WebSymbols registry stack overflows randomly
    this.enableIdempotenceChecksOnEveryCache()
  }

  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "deprecated/attributes"
  }

  private fun resolveReference(signature: String): PsiElement {
    return myFixture.resolveReference(signature)
  }

  private fun resolveWebSymbolReference(signature: String): WebSymbol {
    return myFixture.resolveWebSymbolReference(signature)
  }

  private fun resolveToWebSymbolSource(signature: String): PsiElement {
    return myFixture.resolveToWebSymbolSource(signature)
  }

  private fun assertUnresolvedReference(signature: String) {
    AngularTestUtil.assertUnresolvedReference(signature, myFixture)
  }

  fun testSrcBinding20() {
    myFixture.configureByFiles("srcBinding.html", "package.json")
    myFixture.enableInspections(RequiredAttributesInspection::class.java,
                                HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.checkHighlighting()
  }

  fun testEventHandlers2() {
    myFixture.configureByFiles("event.html", "package.json")
    myFixture.enableInspections(RequiredAttributesInspection::class.java,
                                HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.checkHighlighting()
  }

  fun testEventHandlersStandardCompletion2() {
    myFixture.configureByFiles("event.html", "custom.ts", "package.json")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "(mouseover)")
    myFixture.moveToOffsetBySignature("<some-tag <caret>>")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "(mouseover)")
  }

  fun testBindingStandardCompletion2() {
    myFixture.configureByFiles("bindingHtml.html", "package.json")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "[value]")
  }

  fun testStandardCompletion() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureByFiles("custom.ts", "package.json")
    myFixture.configureByText("test.html", "<some-tag <caret>")
    myFixture.completeBasic()
    myFixture.checkListByFile(myFixture.renderLookupItems(true, true, true, false)
                                .filter { !it.contains("w3c") && !it.contains("aria-") },
                              "standardCompletion.txt", false)
  }

  fun testTemplateReferenceDeclarations2() {
    myFixture.configureByFiles("variable.html", "custom.ts", "package.json")
    myFixture.enableInspections(RequiredAttributesInspection::class.java,
                                HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.checkHighlighting()
  }

  fun testTemplateReferenceCompletion2() {
    myFixture.configureByFiles("binding.html", "package.json")
    myFixture.completeBasic()
    myFixture.checkResultByFile("binding.after.html")
  }

  fun testVariableCompletion2() {
    myFixture.configureByFiles("ngTemplate.html", "package.json")
    myFixture.completeBasic()
    myFixture.checkResultByFile("ngTemplate.after.html")
  }

  fun testTemplateReferenceCompletion2Inline() {
    myFixture.configureByFiles("binding.ts", "package.json")
    myFixture.completeBasic()
    myFixture.checkResultByFile("binding.after.ts")
  }

  fun testTemplateReferenceSmart2() {
    myFixture.configureByFiles("binding.type.html", "package.json")
    val file = myFixture.getFile()
    val offset = AngularTestUtil.findOffsetBySignature("user<caret>name,", file)
    val ref = PsiTreeUtil.getParentOfType(file.findElementAt(offset), JSReferenceExpression::class.java)
    val processor = JSSimpleTypeProcessor()
    JSTypeEvaluator.evaluateTypes(ref!!, file, processor)
    val type = processor.getType()
    UsefulTestCase.assertInstanceOf(type, JSNamedType::class.java)
    assertEquals("HTMLInputElement", type!!.typeText)
  }

  fun testTemplateReferenceResolve2() {
    myFixture.configureByFiles("binding.after.html", "package.json")
    val resolve = resolveReference("\$event, user<caret>name")
    UsefulTestCase.assertInstanceOf(resolve, Angular2HtmlAttrVariable::class.java)
    assertEquals("binding.after.html", resolve.getContainingFile().getName())
    assertEquals("#username", resolve.getParent().getParent().getText())
  }

  fun testVariableResolve2() {
    myFixture.configureByFiles("ngTemplate.after.html", "package.json")
    val resolve = resolveReference("{{my_<caret>user")
    UsefulTestCase.assertInstanceOf(resolve, Angular2HtmlAttrVariable::class.java)
    assertEquals(Angular2HtmlAttrVariable.Kind.LET, (resolve as Angular2HtmlAttrVariable).kind)
    assertEquals("ngTemplate.after.html", resolve.getContainingFile().getName())
    assertEquals("let-my_user", resolve.getParent().getParent().getText())
  }

  fun testTemplateReferenceResolve2Inline() {
    myFixture.configureByFiles("binding.after.ts", "package.json")
    val resolve = resolveReference("in<caret>put_el.")
    UsefulTestCase.assertInstanceOf(resolve, Angular2HtmlAttrVariable::class.java)
    assertEquals(Angular2HtmlAttrVariable.Kind.REFERENCE, (resolve as Angular2HtmlAttrVariable).kind)
    assertEquals("binding.after.ts", resolve.getContainingFile().getName())
    assertEquals("#input_el", resolve.getParent().getParent().getText())
  }

  fun testBindingCompletion2TypeScript() {
    myFixture.configureByFiles("object_binding.html", "package.json", "object.ts")
    myFixture.completeBasic()
    myFixture.checkResultByFile("object_binding.after.html")
  }

  fun testBindingResolve2TypeScript() {
    myFixture.configureByFiles("object_binding.after.html", "package.json", "object.ts")
    val resolve = resolveToWebSymbolSource("[mod<caret>el]")
    assertEquals("object.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, JSField::class.java)
  }

  fun testBindingResolve2TypeScriptInputInDecorator() {
    myFixture.copyFileToProject("object_in_dec.ts")
    myFixture.configureByFiles("object_binding.after.html", "package.json")
    val resolve = resolveToWebSymbolSource("[mod<caret>el]")
    assertEquals("object_in_dec.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, JSField::class.java)
    val cls = PsiTreeUtil.getContextOfType(resolve, TypeScriptClass::class.java)!!
    val component: Angular2Directive = getComponent(cls)!!
    assertEquals(ContainerUtil.newHashSet("model", "id", "oneTime", "oneTimeList"),
                 component.inputs.mapTo(HashSet(), Angular2DirectiveProperty::name))
    assertEquals(setOf("complete"),
                 component.outputs.mapTo(HashSet(), Angular2DirectiveProperty::name))
    assertEquals(ContainerUtil.newHashSet("testAttrOne", "testAttrTwo", "testAttrThree"),
                 component.attributes.mapTo(HashSet(), Angular2DirectiveAttribute::name))
    myFixture.moveToOffsetBySignature("[model]=\"\"<caret>")
    myFixture.type(' ')
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "testAttrOne", "testAttrTwo", "testAttrThree")
  }

  fun testStaticAttributes() {
    myFixture.copyFileToProject("object_in_dec.ts")
    myFixture.configureByFiles("static_attributes.html", "package.json")
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.checkHighlighting()
  }

  fun testBindingCompletionViaBase2TypeScript() {
    myFixture.configureByFiles("object_binding_via_base.html", "package.json", "inheritor.ts", "object.ts")
    myFixture.completeBasic()
    myFixture.checkResultByFile("object_binding_via_base.after.html")
  }

  fun testBindingResolveViaBase2TypeScript() {
    myFixture.configureByFiles("object_binding_via_base.after.html", "package.json", "inheritor.ts", "object.ts")
    val resolve = resolveToWebSymbolSource("[mod<caret>el]")
    assertEquals("object.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, JSField::class.java)
  }

  fun testBindingOverride2CompletionTypeScript() {
    myFixture.configureByFiles("object_binding.html", "package.json", "objectOverride.ts")
    myFixture.completeBasic()
    myFixture.checkResultByFile("object_binding.after.html")
  }

  fun testBindingOverrideResolve2TypeScript() {
    myFixture.configureByFiles("object_binding.after.html", "package.json", "objectOverride.ts")
    val resolve = resolveToWebSymbolSource("[mod<caret>el]")
    assertEquals("objectOverride.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, JSField::class.java)
  }

  fun testBindingAttributeCompletion2TypeScript() {
    myFixture.configureByFiles("attribute_binding.html", "package.json", "object.ts")
    myFixture.completeBasic()
    myFixture.checkResultByFile("attribute_binding.after.html")
  }

  fun testBindingAttributeResolve2TypeScript() {
    myFixture.configureByFiles("attribute_binding.after.html", "package.json", "object.ts")
    val resolve = resolveToWebSymbolSource("[mod<caret>el]")
    assertEquals("object.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, JSField::class.java)
  }

  fun testOneTimeBindingAttributeCompletion2TypeScript() {
    myFixture.configureByFiles("attribute_one_time_binding.html", "package.json", "object.ts")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "model", "oneTime", "oneTimeList")
  }

  fun testOneTimeBindingAttributeCompletion3TypeScript() {
    myFixture.configureByFiles("attribute_one_time_binding.html", "package.json", "object.ts")
    myFixture.completeBasic()
    myFixture.type("onetli\n")
    myFixture.checkResultByFile("attribute_one_time_binding.after.html")
  }

  fun testOneTimeBindingAttributeResolve2TypeScript() {
    myFixture.configureByFiles("attribute_one_time_binding.after.html", "package.json", "object.ts")
    val resolve = resolveToWebSymbolSource("one<caret>TimeList")
    assertEquals("object.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, JSField::class.java)
  }

  fun testOneTimeBindingAttributeResolve2JavaScript() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_MATERIAL_7_2_1)
    myFixture.configureByFiles("compiled_binding.after.html")
    val resolve = resolveToWebSymbolSource("col<caret>or")
    assertEquals("color.d.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, TypeScriptPropertySignature::class.java)
    assertEquals("""/** Theme color palette for the component. */
    color: ThemePalette""", resolve.getText())
  }

  fun testOneTimeBindingAttributeCompletion2JavaScript() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_MATERIAL_7_2_1)
    myFixture.configureByFiles("compiled_binding.html")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "color")
  }

  fun testOneTimeBindingAttributeCompletion2ES6() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_MATERIAL_7_2_1)
    myFixture.configureByFiles("compiled_binding.html")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "color")
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!, "tabIndex")
  }

  fun testBindingAttributeFunctionCompletion2TypeScript() {
    myFixture.configureByFiles("attribute_binding.html", "package.json", "object_with_function.ts")
    myFixture.completeBasic()
    myFixture.checkResultByFile("attribute_binding.after.html")
  }

  fun testBindingAttributeFunctionResolve2TypeScript() {
    myFixture.configureByFiles("attribute_binding.after.html", "package.json", "object_with_function.ts")
    val resolve = resolveToWebSymbolSource("[mod<caret>el]")
    assertEquals("object_with_function.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, JSFunction::class.java)
  }

  fun testEventHandlerCompletion2TypeScript() {
    myFixture.configureByFiles("object_event.html", "package.json", "object.ts")
    myFixture.completeBasic()
    myFixture.checkResultByFile("object_event.after.html")
  }

  fun testEventHandlerResolve2TypeScript() {
    myFixture.configureByFiles("object_event.after.html", "package.json", "object.ts")
    val resolve = resolveToWebSymbolSource("(co<caret>mplete)")
    assertEquals("object.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, JSField::class.java)
  }

  fun testEventHandlerOverrideCompletion2TypeScript() {
    myFixture.configureByFiles("object_event.html", "package.json", "objectOverride.ts")
    myFixture.completeBasic()
    myFixture.checkResultByFile("object_event.after.html")
  }

  fun testEventHandlerOverrideResolve2TypeScript() {
    myFixture.configureByFiles("object_event.after.html", "package.json", "objectOverride.ts")
    val resolve = resolveToWebSymbolSource("(co<caret>mplete)")
    assertEquals("objectOverride.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, JSField::class.java)
  }

  fun testForCompletion2TypeScript() {
    myFixture.configureByFiles("for2.html", "ng_for_of.ts", "package.json")
    val offsetBySignature = AngularTestUtil.findOffsetBySignature("ngF<caret>", myFixture.getFile())
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature)
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "ngFor", "[ngForOf]")
  }

  fun testForOfResolve2Typescript() {
    myFixture.configureByFiles("for2.html", "ng_for_of.ts", "package.json")
    val resolve = resolveWebSymbolReference("ngF<caret>")
    assertEquals("ng_for_of.ts", resolve.psiContext!!.getContainingFile().getName())
    assertEquals("ngFor", resolve.name)
    assertEquals("@Directive({selector: '[ngFor][ngForOf]'})", AngularTestUtil.getDirectiveDefinitionText(resolve.psiContext))
  }

  fun testForCompletion2Javascript() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_4_0_0)
    myFixture.configureByFiles("for2.html")
    val offsetBySignature = AngularTestUtil.findOffsetBySignature("ngF<caret>", myFixture.getFile())
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature)
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "ngFor", "[ngForOf]")
  }

  fun testIfCompletion4JavascriptUmd() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_4_0_0)
    myFixture.configureByFiles("if4.html")
    val offsetBySignature = AngularTestUtil.findOffsetBySignature("*<caret>", myFixture.getFile())
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature)
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "*ngIf")
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!, "ngIf")
  }

  fun testForTemplateCompletion2Javascript() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_4_0_0)
    myFixture.configureByFiles("for2Template.html")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "*ngFor")
  }

  fun testForOfResolve2Javascript() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_4_0_0)
    myFixture.configureByFiles("for2.html")
    val resolve = myFixture.resolveWebSymbolReference("ngF<caret>")
    assertEquals("ng_for_of.d.ts", resolve.psiContext!!.getContainingFile().getName())
  }

  fun testTemplateUrl20Completion() {
    myFixture.configureByFiles("custom.ts", "package.json", "custom.html")
    val offsetBySignature = AngularTestUtil.findOffsetBySignature("templateUrl: '<caret>", myFixture.getFile())
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature)
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "custom.ts", "package.json", "custom.html")
  }

  fun testTemplateUrl20Resolve() {
    myFixture.configureByFiles("custom.template.ts", "package.json", "custom.html")
    val resolve = resolveReference("templateUrl: '<caret>")
    UsefulTestCase.assertInstanceOf(resolve, PsiFile::class.java)
    assertEquals("custom.html", (resolve as PsiFile).getName())
  }

  fun testStyleUrls20Completion() {
    myFixture.configureByFiles("custom.ts", "package.json", "custom.html")
    val offsetBySignature = AngularTestUtil.findOffsetBySignature("styleUrls: ['<caret>", myFixture.getFile())
    myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature)
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "custom.ts", "package.json", "custom.html")
  }

  fun testStyleUrls20Resolve() {
    myFixture.configureByFiles("custom.style.ts", "package.json", "custom.html")
    val resolve = resolveReference("styleUrls: ['<caret>")
    UsefulTestCase.assertInstanceOf(resolve, PsiFile::class.java)
    assertEquals("custom.html", (resolve as PsiFile).getName())
  }

  fun testBindingNamespace() {
    myFixture.configureByFiles("bindingNamespace.html", "package.json")
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java,
                                XmlUnboundNsPrefixInspection::class.java)
    myFixture.checkHighlighting()
  }

  fun testEventNamespace() {
    myFixture.configureByFiles("eventNamespace.html", "package.json")
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java,
                                XmlUnboundNsPrefixInspection::class.java)
    myFixture.checkHighlighting()
  }

  fun testCaseCompletion2() {
    myFixture.configureByFiles("case.html", "ng_for_of.ts", "package.json")
    myFixture.completeBasic()
    myFixture.checkResultByFile("case.after.html")
  }

  fun testTemplatesCompletion() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_4_0_0)
    myFixture.configureByFiles("templates_completion.html")
    myFixture.completeBasic()
    assertEquals(mutableListOf("*ngIf", "*ngSwitchCase", "*ngSwitchDefault"),
                 ContainerUtil.sorted(myFixture.getLookupElementStrings()!!))
  }

  fun testTemplatesCompletion2() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_16_0_0_NEXT_4)
    myFixture.configureByFiles("templates_completion2.html")
    myFixture.completeBasic()
    assertEquals(
      mutableListOf("*ngComponentOutlet", "*ngPluralCase", "*ngSwitchCase", "[ngClass]", "[ngComponentOutlet]", "ngComponentOutlet"),
      ContainerUtil.sorted(myFixture.getLookupElementStrings()!!))
  }

  fun testMaterialSelectors() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_MATERIAL_7_2_1)
    myFixture.configureByFiles("material.html")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "mat-icon-button", "mat-raised-button")
  }

  fun testComplexSelectorList2() {
    configureLink(myFixture, Angular2TestModule.IONIC_ANGULAR_3_0_1)
    myFixture.configureByFiles("div.html")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "ion-item")
  }

  fun testSelectorListSpaces() {
    myFixture.configureByFiles("spaces.html", "package.json", "spaces.ts")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "other-attr")
  }

  fun testSelectorListSpaces2() {
    myFixture.configureByFiles("spaces.html", "package.json", "spaces.ts")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "other-attr")
  }

  fun testId() {
    myFixture.enableInspections(XmlInvalidIdInspection::class.java)
    myFixture.configureByFiles("id.html", "package.json", "object.ts")
    myFixture.checkHighlighting()
  }

  fun testViewChildReferenceNavigation() {
    val reference = myFixture.getReferenceAtCaretPosition("viewChildReference.ts", "package.json")
    assertNotNull(reference)
    val el = reference!!.resolve()
    assertNotNull(el)
    assertEquals("#area", el!!.getParent().getParent().getText())
  }

  fun testViewChildrenReferenceNavigation() {
    val reference = myFixture.getReferenceAtCaretPosition("viewChildrenReference.ts", "package.json")
    assertNotNull(reference)
    val el = reference!!.resolve()
    assertNotNull(el)
    assertEquals("#area", el!!.getParent().getParent().getText())
  }

  fun testViewChildReferenceCodeCompletion() {
    assertEquals(mutableListOf("area", "area2", "area3"),
                 myFixture.getCompletionVariants("viewChildReference.ts", "package.json"))
  }

  fun testViewChildReferenceNavigationHTML() {
    val reference = myFixture.getReferenceAtCaretPosition("viewChildReferenceHTML.ts", "viewChildReferenceHTML.html", "package.json")
    assertNotNull(reference)
    val el = reference!!.resolve()
    assertNotNull(el)
    assertEquals("viewChildReferenceHTML.html", el!!.getContainingFile().getName())
    assertEquals("#area", el.getParent().getParent().getText())
  }

  fun testViewChildReferenceCodeCompletionHTML() {
    assertEquals(mutableListOf("area", "area2"),
                 myFixture.getCompletionVariants("viewChildReferenceHTML.ts", "viewChildReferenceHTML.html", "package.json"))
  }

  fun testViewChildrenReferenceCodeCompletionHTML() {
    assertEquals(mutableListOf("area2", "area3"),
                 myFixture.getCompletionVariants("viewChildrenReferenceHTML.ts", "viewChildrenReferenceHTML.html", "package.json"))
  }

  fun testI18NAttr() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_MATERIAL_8_2_3_MIXED)
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("i18n.html")
    myFixture.checkHighlighting(true, false, true)
  }

  fun testNgNoValidateReference() {
    myFixture.configureByFiles("ngNoValidate.html", "ng_no_validate_directive.ts", "package.json")
    val resolve = resolveWebSymbolReference("ng<caret>NativeValidate")
    UsefulTestCase.assertInstanceOf(resolve.unwrapAllDelegates(), Angular2DirectiveSelectorSymbol::class.java)
    assertEquals("ng_no_validate_directive.ts", resolve.psiContext!!.getContainingFile().getName())
  }

  fun testSelectorBasedAttributesCompletion() {
    myFixture.configureByFiles("selectorBasedAttributes.ts", "package.json")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "myInput", "[myInput]",
                                          "(myOutput)",
                                          "[mySimpleBindingInput]", "mySimpleBindingInput",
                                          "myPlain",
                                          "myInOut", "[myInOut]", "[(myInOut)]",
                                          "fake", "[fake]", "[(fake)]",
                                          "(fakeChange)")
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!,
                                       "(myInput)", "[(myInput)]",
                                       "myOutput", "[myOutput]", "[(myOutput)]",
                                       "(mySimpleBindingInput)", "[(mySimpleBindingInput)]",
                                       "[myPlain]", "(myPlain)", "[(myPlain)]",
                                       "(myInOut)",
                                       "myInOutChange", "(myInOutChange)", "[myInOutChange]", "[(myInOutChange)]",
                                       "(fake)",
                                       "fakeChange, [fakeChange], [(fakeChange)]")
  }

  fun testSelectorBasedAttributesNavigation() {
    myFixture.configureByFiles("selectorBasedAttributes.ts", "package.json")
    val attrWrap = listOf(Pair("", ""), Pair("[", "]"), Pair("(", ")"), Pair("[(", ")]"))
    for ((name, checks) in java.util.Map.of( // <simple><input><output><inout>
      // x -> no resolve, p -> resolve to property, s -> resolve to selector
      "myInput", "ppxx",
      "mySimpleBindingInput", "ppxx",
      "myPlain", "sxxx",
      "myOutput", "xxpx",
      "myInOut", "ppxp",
      "myInOutChange", "xxpx",
      "fake", "ccxc",
      "fakeChange", "sxcx")) {
      for (i in attrWrap.indices) {
        val wrap = attrWrap[i]
        val ref = myFixture.multiResolveWebSymbolReference(wrap.first + "<caret>" + name + wrap.second + "=")
          .filter { s: WebSymbol ->
            s.properties[Angular2WebSymbolsQueryConfigurator.PROP_ERROR_SYMBOL] != true
            && s.properties[Angular2WebSymbolsQueryConfigurator.PROP_BINDING_PATTERN] != true
          }
        val sources = ref.map { it.psiContext }
        val messageStart = "Attribute " + wrap.first + name + wrap.second
        when (checks[i]) {
          'x' -> assertEquals("$messageStart should not resolve", emptyList<Any>(), ref)
          'p' -> {
            assertNotNull("$messageStart should have reference", ref)
            assert(
              sources.all { TypeScriptField::class.java.isInstance(it) }) {
              messageStart + " should resolve to TypeScriptField instead of " +
              sources
            }
          }
          's' -> {
            assertNotNull("$messageStart should have reference", ref)
            assert(
              ref.all { it.unwrapAllDelegates() is Angular2DirectiveSelectorSymbol }) {
              messageStart + " should resolve to Angular2DirectiveSelectorElement instead of " +
              ref
            }
          }
          'c' -> {
            assertNotNull("$messageStart should have reference", ref)
            assert(
              sources.all { TypeScriptClass::class.java.isInstance(it) }) {
              messageStart + " should resolve to Angular2DirectiveSelectorElement instead of " +
              sources
            }
          }
          else -> throw IllegalStateException("wrong char: " + checks[i])
        }
      }
    }
  }

  fun testRequiredInputs() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("requiredInputs.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testExportAs() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection::class.java,
                                JSUnresolvedReferenceInspection::class.java,
                                TypeScriptValidateTypesInspection::class.java)
    myFixture.configureByFiles("exportAs.ts", "package.json")
    myFixture.checkHighlighting(true, false, true)
  }

  fun testMultipleExportAsNames() {
    Registry.get("ide.completion.variant.limit").setValue(10000, testRootDisposable)
    myFixture.configureByFiles("exportAsMultipleNames.ts", "package.json")
    for (name in mutableListOf("r", "f", "g")) {
      myFixture.moveToOffsetBySignature("{{ $name.<caret> }}")
      myFixture.completeBasic()
      if (name == "g") {
        UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "length", "type")
      }
      else {
        UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "foo")
        UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!, "split", "length", "type")
      }
    }
  }

  fun testOneTimeBindingOfPrimitives() {
    myFixture.configureByFiles("one_time_binding.html", "one_time_binding.ts", "package.json")
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.checkHighlighting(true, false, true)
  }

  fun testStandardPropertiesOnComponent() {
    myFixture.configureByFiles("std_props_on_component.html", "one_time_binding.ts", "package.json")
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.checkHighlighting(true, false, true)
  }

  fun testCaseInsensitiveAttrNames() {
    myFixture.configureByFiles("case_insensitive.html", "one_time_binding.ts", "package.json")
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.checkHighlighting(true, false, true)
  }

  fun testCodeCompletionWithNotSelector() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_ROUTER_4_0_0)
    myFixture.configureByFiles("contentAssistWithNotSelector.html")
    myFixture.completeBasic()
  }

  fun testNoLifecycleHooksInCodeCompletion() {
    myFixture.configureByFiles("lifecycleHooks.ts", "package.json")
    myFixture.completeBasic()
    assertEquals(listOf("\$any", "testOne", "testTwo", "testing"),
                 ContainerUtil.sorted(AngularTestUtil.renderLookupItems(myFixture, false, false, true)))
  }

  fun testDecoratorInGetter() {
    myFixture.configureByFiles("decoratorInGetter.ts", "package.json")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "[age]")
  }

  fun testStandardTagProperties() {
    myFixture.configureByFiles("standardTagProperties.ts", "package.json")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "[autofocus]", "[readOnly]", "[selectionDirection]", "[innerHTML]",
                                          "(auxclick)", "(blur)", "(click)", "(paste)", "(webkitfullscreenchange)")
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!,
                                       "innerHTML")
  }

  fun testNgIfAsCodeCompletion() {
    myFixture.configureByFiles("ngIfAs.ts", "package.json")
    myFixture.completeBasic()
    myFixture.checkResultByFile("ngIfAs.after.ts")
  }

  /**
   * Tests an older version of library
   *
   * @see Angular2ExpressionTypesInspectionTest.testNgrxLetContextGuard
   */
  fun testNgrxLetAsContextGuard() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_COMMON_13_3_5)
    myFixture.configureByFiles("ngrxLetAs.ts", "ngrxLet.ts", "package.json", "tsconfig.json")
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.checkHighlighting(true, false, true)
  }

  fun testCodeCompletionItemsTypes() {
    myFixture.configureByFiles("attributeTypes.ts", "lib.dom.d.ts", "package.json")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(
      AngularTestUtil.renderLookupItems(myFixture, false, true),
      "plainBoolean#boolean",
      "[plainBoolean]#boolean",
      "simpleStringEnum#MyType",
      "[simpleStringEnum]#MyType",
      "(my-event)#MyEvent",
      "(problematicOutput)#T",
      "(complex-event)#MyEvent | MouseEvent",
      "(click)#MouseEvent",
      "(blur)#FocusEvent",
      "(focusin)#FocusEvent",
      "(copy)#ClipboardEvent",
      "(transitionend)#TransitionEvent",
      "[innerHTML]#string"
    )
  }

  fun testMatchedDirectivesProperties() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_4_0_0, Angular2TestModule.ANGULAR_FORMS_4_0_0)
    myFixture.configureByFiles("attributeTypes.ts", "lib.dom.d.ts")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(
      AngularTestUtil.renderLookupItems(myFixture, true, false),
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
    )
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!, "[ngModel]", "ngModel", "[matchedPlainBoolean]")
  }

  fun testCodeCompletionOneTimeSimpleStringEnum() {
    myFixture.configureByFiles("attributeTypes.ts", "package.json")
    myFixture.completeBasic()
    myFixture.type("simpleS\n")
    myFixture.completeBasic()
    UsefulTestCase.assertSameElements(myFixture.getLookupElementStrings()!!, "off", "polite", "assertive")
  }

  fun testCodeCompletionOneTimeSimpleStringEnumSetter() {
    myFixture.configureByFiles("attributeTypes.ts", "package.json")
    myFixture.completeBasic()
    myFixture.type("setterSi\n")
    myFixture.completeBasic()
    UsefulTestCase.assertSameElements(myFixture.getLookupElementStrings()!!, "off", "polite", "assertive")
  }

  fun testCodeCompletionOneTimeBoolean() {
    myFixture.configureByFiles("attributeTypes.ts", "package.json")
    myFixture.completeBasic()
    //test no auto-value completion
    myFixture.type("plainB\n=")
    myFixture.completeBasic()
    //test type
    UsefulTestCase.assertSameElements(myFixture.getLookupElementStrings()!!, "plainBoolean", "true", "false")
  }

  fun testCodeCompletionDefaultJSEventType() {
    TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    myFixture.configureByFiles("attributeTypes.ts", "lib.dom.d.ts", "package.json")
    myFixture.completeBasic()
    myFixture.type("(clic\n\$event.")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "x", "y", "layerX", "layerY", "altKey", "button",
                                          "clientX", "clientY", "isTrusted", "timeStamp")
  }

  fun testAttrCompletions() {
    myFixture.configureByFiles("attrTest.ts", "package.json")
    myFixture.completeBasic()
    myFixture.type("att\n")
    myFixture.type("acc\n")
    val element = resolveToWebSymbolSource("[attr.ac<caret>cesskey]=\"\"")
    assertEquals("common.rnc", element.getContainingFile().getName())
  }

  fun testAttrCompletionsCustomTag() {
    myFixture.configureByFiles("attrTestCustom.ts", "package.json")
    myFixture.completeBasic()
    myFixture.type("[attr.")
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "about]", "accesskey]",
                                          "id]", "style]", "title]",
                                          "aria-atomic]")
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!,
                                       "innerHTML]")
    myFixture.type("acc\n")
    val element = resolveToWebSymbolSource("[attr.ac<caret>cesskey]=\"\"")
    assertEquals("common.rnc", element.getContainingFile().getName())
  }

  fun testAttrCompletions2() {
    myFixture.configureByFiles("div.html", "package.json")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "[attr.")
    myFixture.type("att\n")
    // TODO - make web-types insert ']' - should be "<div [attr.]" here
    assertEquals("<div [attr.", myFixture.getFile().getText())
    myFixture.type("aat\n")
    val element = resolveToWebSymbolSource("[attr.aria-atomic<caret>]=\"\"")
    assertEquals("aria.rnc", element.getContainingFile().getName())
  }

  fun testCanonicalCompletion() {
    myFixture.configureByFiles("div.html", "package.json")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "bind-", "bindon-", "on-", "ref-")
    myFixture.type("bind-")
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "tabIndex", "innerHTML")
    myFixture.type("iH\n")
    val element = resolveToWebSymbolSource("bind-inner<caret>HTML")
    assertEquals("lib.dom.d.ts", element.getContainingFile().getName())
  }

  fun testAttrCompletionsCanonical() {
    myFixture.configureByFiles("attrTest.ts", "package.json")
    myFixture.completeBasic()
    myFixture.type("bind-")
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "attr.")
    myFixture.type("at\naat\n")
    val element = resolveToWebSymbolSource("bind-attr.aria-atomic<caret>=\"\"")
    assertEquals("aria.rnc", element.getContainingFile().getName())
  }

  fun testExtKeyEvent() {
    myFixture.configureByFiles("attrTest.ts", "package.json")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "(keyup.", "(keydown.")
    myFixture.type("keyd.\n")
    assertEquals("(keydown.", myFixture.getFile().findElementAt(myFixture.getCaretOffset() - 1)!!.getText())
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "meta.", "control.", "shift.", "alt.", "escape)", "home)")
    myFixture.type("alt.")
    assertEquals("(keydown.alt.", myFixture.getFile().findElementAt(myFixture.getCaretOffset() - 1)!!.getText())
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "meta.", "control.", "escape)", "home)")
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!,
                                       "alt.")
    myFixture.type("ins\n")
    assertEquals("<div (keydown.alt.insert)=\"\"", myFixture.getFile().getText())
  }

  fun testExtKeyEventCanonical() {
    myFixture.configureByFiles("attrTest.ts", "package.json")
    myFixture.completeBasic()
    myFixture.type("on-")
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "keyup.", "keydown.")
    myFixture.type("keyu.\n")
    assertEquals("on-keyup.", myFixture.getFile().findElementAt(myFixture.getCaretOffset() - 1)!!.getText())
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "meta.", "control.", "shift.", "alt.", "escape", "home")
    myFixture.type("alt.")
    assertEquals("on-keyup.alt.", myFixture.getFile().findElementAt(myFixture.getCaretOffset() - 1)!!.getText())
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "meta.", "control.", "escape", "home")
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!,
                                       "alt.")
    myFixture.type("m.\n")
    assertEquals("on-keyup.alt.meta.", myFixture.getFile().findElementAt(myFixture.getCaretOffset() - 1)!!.getText())
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!,
                                       "alt.", "meta.")
    myFixture.type("esc\n")
    val source = resolveWebSymbolReference("on-keyup.alt.meta.<caret>escape=\"\"")
    assertEquals("Extended event special key", source.name)
  }

  fun testExtKeyEventsInspections() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.configureByFiles("extKeyEvents.html", "custom.ts", "package.json")
    myFixture.checkHighlighting(true, false, true)
  }

  fun testAnimationCallbacks() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("animationCallbacks.html", "animationCallbacks.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testAttributeNameMapping() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.configureByFiles("attributeNameMapping.html", "package.json")
    myFixture.checkHighlighting()
  }

  fun testMultiSelectorTemplateBindings() {
    myFixture.configureByFiles("multi-selector-template-bindings.html", "multi-selector-template-bindings.ts", "package.json")
    myFixture.completeBasic()
    assertEquals(mutableListOf("*appIf", "*appUnless"),
                 ContainerUtil.sorted(myFixture.getLookupElementStrings()!!))
    assertEquals("multi-selector-template-bindings.ts",
                 resolveToWebSymbolSource("*app<caret>If=").getContainingFile().getName())
    assertEquals("multi-selector-template-bindings.ts",
                 resolveToWebSymbolSource("*app<caret>Unless=").getContainingFile().getName())
    assertUnresolvedReference("*app<caret>Foo=")
  }

  fun testTypeAttrWithFormsCompletion() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_4_0_0, Angular2TestModule.ANGULAR_FORMS_4_0_0)
    myFixture.configureByFiles("typeAttrWithForms.html", "typeAttrWithForms.ts")
    myFixture.moveToOffsetBySignature("<button <caret>>")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "type")
    myFixture.moveToOffsetBySignature("<input <caret>/>")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "type")
  }

  fun testMultiResolve() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("multi-resolve.html", "multi-resolve.ts", "package.json")
    myFixture.checkHighlighting()
    val data = mapOf(
      Pair("i<caret>d=", mutableListOf("id", "id: /*c1*/ string")),
      Pair("[i<caret>d]=", mutableListOf("""
        |/**
        |     * Returns the value of element's id content attribute. Can be set to change it.
        |     *
        |     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Element/id)
        |     */
        |    id: string
        """.trimMargin(), "id: /*c1*/ string")),
      Pair("[attr.i<caret>d]=", listOf("id")),
      Pair("b<caret>ar=", mutableListOf("bar: /*c1*/ number", "bar: /*c2*/ number")),
      Pair("[b<caret>ar]=", mutableListOf("bar: /*c1*/ number", "bar: /*c2*/ number")),
      Pair("bo<caret>o=", mutableListOf("boo: /*c1*/ number", "boo: /*c2*/ string")),
      Pair("[b<caret>oo]=", mutableListOf("boo: /*c1*/ number", "boo: /*c2*/ string"))
    )
    for ((location, value) in data) {
      UsefulTestCase.assertSameElements(
        myFixture.multiResolveWebSymbolReference(location)
          .map {
            if (it is PsiSourcedWebSymbol) {
              val source = it.source
              when {
                source == null -> "<null>"
                source.getText() != null -> source.getText()
                else -> source.getParent().getText()
              }
            }
            else {
              it.name
            }
          }
          .sorted(),
        ContainerUtil.sorted(value))
    }
  }

  fun testNgTemplateOutletCompletion() {
    myFixture.configureByFiles("ng-template-outlet-test.html", "ng_template_outlet.ts", "ng_if.ts", "package.json")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, mutableListOf("*ngIf", "*ngTemplateOutlet"))
  }

  fun testNgContentCompletion() {
    myFixture.configureByFiles("ng-content-completion.html", "package.json")
    myFixture.completeBasic()
    assertEquals(listOf("select", "xml:base", "xml:lang", "xml:space"), myFixture.getLookupElementStrings()!!)
  }

  fun testNgContentInspection() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.configureByFiles("ng-content-inspection.html", "package.json")
    myFixture.checkHighlighting()
  }

  fun testDirectiveAttributesCompletion() {
    myFixture.configureByFiles("directive_attrs_completion.ts", "package.json")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "foo")
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!, "bar", "[bar]", "[foo]", "test", "[test]")
    myFixture.type("foo=\" ")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "bar", "test", "[test]")
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!, "[bar]", "foo", "[foo]")
  }

  fun testNgNonBindable() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("ngNonBindable.html", "package.json")
    myFixture.checkHighlighting()
  }

  fun testIonicAttributes() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    configureLink(myFixture, Angular2TestModule.IONIC_ANGULAR_4_1_1)
    myFixture.configureByFiles("ionicAttributes.html")
    myFixture.checkHighlighting()
  }

  fun testSvgAttributes() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("svg-test.html", "svg-test.ts", "package.json")
    myFixture.checkHighlighting()
    myFixture.moveToOffsetBySignature("<svg:clipPath id=\"clip\"><caret>")
    myFixture.type("<svg:circle [attr.")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "cx]", "cy]", "visibility]", "text-rendering]")
  }

  fun testCustomDataAttributes() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("data-attributes.html", "object.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testInputTypeCompletion() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_FORMS_4_0_0)
    myFixture.configureByText("input-type.html", "<input type=\"<caret>\"")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!,
                                          "button", "checkbox", "color", "date", "datetime-local", "email", "file", "hidden", "image",
                                          "month",
                                          "number", "password", "radio", "range", "reset", "search", "submit", "tel", "text", "time", "url",
                                          "week")
  }

  fun testI18nCompletion() {
    myFixture.configureByFile("package.json")
    myFixture.configureByText("i18n.html", "<div foo='12' <caret>")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "i18n-")
    myFixture.type("i1-\n")
    assertEquals(listOf("foo"), myFixture.getLookupElementStrings()!!)
    myFixture.type("\n ")
    myFixture.completeBasic()
    // TODO - remove "Absent attribute name" from angular web-types
    //assertDoesntContain(myFixture.getLookupElementStrings()!!, "i18n-");
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "i18n")
  }

  fun testI18nResolve() {
    myFixture.configureByFile("package.json")
    myFixture.configureByText("i18n.html", "<div foo='12' i18n-f<caret>oo>")
    val source = resolveToWebSymbolSource("i18n-f<caret>oo>")
    assertEquals("foo='12'", source.getText())
  }

  fun testHammerJS() {
    myFixture.configureByFile("package.json")
    myFixture.configureByText("hammer.html", "<div <caret>")
    myFixture.completeBasic()
    myFixture.type("(")
    UsefulTestCase.assertContainsElements(AngularTestUtil.renderLookupItems(myFixture, false, true),
                                          "(pan)#HammerInput", "(panstart)#HammerInput", "(pinch)#HammerInput", "(tap)#HammerInput")
    myFixture.type("pan\n\" on-")
    myFixture.completeBasic()
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!, "pan")
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "panstart", "pinch", "tap")
    myFixture.type("pansta\n")
    myFixture.checkResult("<div (pan)=\"\" on-panstart=\"<caret>\"")
  }

  fun testTransitionEvents() {
    myFixture.configureByFile("package.json")
    myFixture.configureByText("test.html", "<div (tr<caret>")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "(transitionend)", "(transitionstart)")
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByText("test.html",
                              "<div (transitionend)='<error descr=\"Unresolved function or method call()\">call</error>(\$event)'></div>")
    myFixture.checkHighlighting()
  }

  fun testElementShims() {
    myFixture.copyDirectoryToProject("element_shims", ".")
    myFixture.configureFromTempProjectFile("selectChangeEvent.html")
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.checkHighlighting()
  }

  fun testCustomUserEvents() {
    myFixture.copyDirectoryToProject("custom-user-events", ".")
    myFixture.configureFromTempProjectFile("customEvents.html")
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.checkHighlighting()
  }

  fun testFxLayout() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_9_1_1_MIXED, Angular2TestModule.ANGULAR_FLEX_LAYOUT_13_0_0)
    myFixture.configureByFiles("fxLayout.html", "package.json")
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.checkHighlighting()
  }

  fun testHtmlAttributesInspections() {
    myFixture.configureByFiles("htmlAttributesInspections.html", "package.json")
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.checkHighlighting()
  }

  fun testCdkDirectivesHighlighting() {
    myFixture.configureByFiles("cdkDirectives.html", "cdkDirectives.ts")
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CDK_14_2_0)
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.checkHighlighting()
  }

  fun testCdkDirectivesCompletion() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CDK_14_2_0)
    myFixture.configureByFile(getTestName(true) + ".html")
    myFixture.completeBasic()
    myFixture.checkListByFile(myFixture.renderLookupItems(true, true), getTestName(true) + ".expected.txt", false)
  }

  fun testStyleUnitLengthCompletion() {
    myFixture.configureByFiles("package.json")
    doCompletionItemsTest(myFixture, "styleUnitLengthCompletion.html", false, false)
  }

  fun testStyleUnitLengthHighlighting() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("styleUnitLengthCompletionHighlighting.html", "package.json")
    myFixture.checkHighlighting()
  }

  fun testMatSortHeader() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_MATERIAL_14_2_5_MIXED)
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("matSortHeader.html")
    myFixture.checkHighlighting()
  }

  fun testImgSrcWithNg15() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_CORE_15_1_5, Angular2TestModule.ANGULAR_COMMON_15_1_5)
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFile("imgSrcWithNg15.html")
    myFixture.checkHighlighting()
  }

  fun testNoRequiredBindingsWithoutModuleScope() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_16_0_0_NEXT_4, Angular2TestModule.ANGULAR_COMMON_16_0_0_NEXT_4,
                  Angular2TestModule.ANGULAR_FORMS_16_0_0_NEXT_4)
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFile("noRequiredBindings.html")
    myFixture.checkHighlighting()
  }

  fun testUnknownTagsAttributesInlineTemplate() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("unknownTagsAttributesInlineTemplate.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testNgAcceptInputTypeOverride() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("ngAcceptInputTypeOverride.ts", "package.json")
    myFixture.checkHighlighting()
  }

  /**
   * @see Angular2ExpressionTypesInspectionTest.testNullChecks
   * @see Angular2ExpressionTypesInspectionTest.testNullChecksInline
   */
  fun testTypeMismatchErrorWithOptionalInputs() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("typeMismatchErrorWithOptionalInputs.ts", "package.json", "tsconfig.json")
    myFixture.checkHighlighting()
  }

  fun testHostDirectives() {
    myFixture.configureByFiles("hostDirectives.component.html", "hostDirectives.component.ts", "package.json")
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.checkHighlighting()
  }
}
