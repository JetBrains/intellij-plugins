// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.javascript.BaseJSCompletionTestCase
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptFieldImpl
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptParameterImpl
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.containers.ContainerUtil
import com.intellij.xml.util.CheckDtdReferencesInspection
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.inspections.Angular2TemplateInspectionsProvider
import org.angularjs.AngularTestUtil

class ContextTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass) + "context"
  }

  private fun resolveReference(signature: String): PsiElement {
    return AngularTestUtil.resolveReference(signature, myFixture)
  }

  fun testInlineTemplateCompletion2TypeScript() {
    myFixture.testCompletion("component.ts", "component.after.ts", "package.json")
  }

  fun testInlineTemplateResolve2TypeScript() {
    myFixture.configureByFiles("component.after.ts", "package.json")
    val resolve = resolveReference("=\"onComple<caret>tedButton()")
    assertEquals("component.after.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, JSFunction::class.java)
  }

  fun testInlineTemplateMethodResolve2TypeScript() {
    myFixture.configureByFiles("templateMethod.ts", "package.json", "customer.ts", "customer2.ts")
    val resolve = resolveReference("ca<caret>ll()")
    assertEquals("customer.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, TypeScriptFunction::class.java)
  }

  fun testNonInlineTemplateCompletion2TypeScript() {
    myFixture.testCompletion(
      "template.completion.html", "template.html", "package.json",
      "template.completion.ts")
  }

  fun testNonInlineTemplateResolve2TypeScript() {
    myFixture.configureByFiles("template.html", "package.json", "template.ts")
    val resolve = resolveReference("myCu<caret>")
    assertEquals("template.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, TypeScriptFieldImpl::class.java)
  }

  fun testNonInlineTemplateUsage2TypeScript() {
    myFixture.enableInspections(JSUnusedLocalSymbolsInspection::class.java, JSUnusedGlobalSymbolsInspection::class.java)
    myFixture.configureByFiles("template.usage.ts", "template.usage.html", "package.json")
    myFixture.checkHighlighting()
  }

  fun testNonInlineTemplateMethodResolve2TypeScript() {
    myFixture.configureByFiles("templateMethod.html", "package.json", "templateMethod.ts", "customer.ts", "customer2.ts")
    val resolve = resolveReference("ca<caret>ll()")
    assertEquals("customer.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, TypeScriptFunction::class.java)
  }

  fun testNonInlineTemplateDefinitionResolve2TypeScript() {
    myFixture.configureByFiles("definition.html", "package.json", "definition.ts", "definition2.ts")
    val resolve = resolveReference("tit<caret>le")
    assertEquals("definition.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, TypeScriptFieldImpl::class.java)
  }

  fun testInlineTemplateDefinitionResolve2TypeScript() {
    myFixture.configureByFiles("definition.ts", "package.json", "definition2.ts")
    val resolve = resolveReference("tit<caret>le")
    assertEquals("definition.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, TypeScriptFieldImpl::class.java)
  }

  fun testNonInlineTemplatePropertyResolve2TypeScript() {
    myFixture.configureByFiles("definition2.html", "package.json", "definition2.ts")
    val resolve = resolveReference("check<caret>ed")
    assertEquals("definition2.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, TypeScriptFieldImpl::class.java)
  }

  fun testInlineTemplatePropertyResolve2TypeScript() {
    myFixture.configureByFiles("definition2.ts", "package.json")
    val resolve = resolveReference("check<caret>ed")
    assertEquals("definition2.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, TypeScriptFieldImpl::class.java)
  }

  fun testComponentFieldsFromConstructorResolve() {
    myFixture.configureByFiles("template.constr.html", "template.constr.ts", "package.json")
    val resolve = resolveReference("myCu<caret>stomer")
    assertEquals("template.constr.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, TypeScriptParameterImpl::class.java)
  }

  fun testInlineComponentFieldsFromConstructorCompletion() {
    myFixture.testCompletion(
      "template.constr.completion.ts", "template.constr.completion.after.ts",
      "package.json")
  }

  fun testInlineTemplateCreateFunction2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection::class.java)
    myFixture.getAllQuickFixes("createFunction.ts", "package.json")
    myFixture.launchAction(
      myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")))
    myFixture.checkResultByFile("createFunction.fixed.ts", true)
  }

  fun testInlineTemplateCreateFunctionWithParam2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection::class.java)
    myFixture.getAllQuickFixes("createFunctionWithParam.ts", "package.json")
    myFixture.launchAction(
      myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")))
    myFixture.checkResultByFile("createFunctionWithParam.fixed.ts", true)
  }

  fun testInlineTemplateCreateFunctionEventEmitter2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection::class.java)
    myFixture.getAllQuickFixes("createFunctionEventEmitter.ts", "package.json")
    myFixture.launchAction(
      myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")))
    myFixture.checkResultByFile("createFunctionEventEmitter.fixed.ts", true)
  }

  fun testInlineTemplateCreateFunctionWithType2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection::class.java)
    myFixture.getAllQuickFixes("createFunctionWithType.ts", "package.json")
    myFixture.launchAction(
      myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")))
    myFixture.checkResultByFile("createFunctionWithType.fixed.ts", true)
  }

  fun testInlineTemplateCreateFunctionEventEmitterImplicit2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection::class.java)
    myFixture.getAllQuickFixes("createFunctionEventEmitterImplicit.ts", "package.json")
    myFixture.launchAction(
      myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")))
    myFixture.checkResultByFile("createFunctionEventEmitterImplicit.fixed.ts", true)
  }

  fun testInlineTemplateCreateField2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection::class.java)
    myFixture.getAllQuickFixes("createField.ts", "package.json")
    myFixture.launchAction(myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.field.intention.name", "todo")))
    myFixture.checkResultByFile("createField.fixed.ts", true)
  }

  fun testNonInlineTemplateCreateFunction2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection::class.java)
    myFixture.getAllQuickFixes("createFunction.html", "createFunction.ts", "package.json")
    myFixture.launchAction(
      myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")))
    myFixture.checkResultByFile("createFunction.ts", "createFunction.fixed.ts", true)
  }

  fun testNonInlineTemplateCreateField2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection::class.java)
    myFixture.getAllQuickFixes("createField.html", "createField.ts", "package.json")
    myFixture.launchAction(myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.field.intention.name", "todo")))
    myFixture.checkResultByFile("createField.ts", "createField.fixed.ts", true)
  }

  fun testNonInlineTemplateCreateFunctionDoubleClass2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection::class.java)
    myFixture.getAllQuickFixes("createFunctionDoubleClass.html", "createFunctionDoubleClass.ts", "package.json")
    myFixture.launchAction(
      myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")))
    myFixture.checkResultByFile("createFunctionDoubleClass.ts", "createFunctionDoubleClass.fixed.ts", true)
  }

  fun testCreateFieldWithExplicitPublicModifier() {
    JSTestUtils.testWithTempCodeStyleSettings<RuntimeException>(project) { settings: CodeStyleSettings ->
      settings.getCustomSettings(TypeScriptCodeStyleSettings::class.java).USE_PUBLIC_MODIFIER = true
      myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection::class.java)
      myFixture.configureByFiles("createFieldWithExplicitPublic.html", "createFieldWithExplicitPublic.ts", "package.json")
      myFixture.launchAction(
        myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.field.intention.name", "unresolved")))
      myFixture.checkResultByFile("createFieldWithExplicitPublic.ts", "createFieldWithExplicitPublic.fixed.ts", true)
    }
  }

  fun testFixSignatureMismatchFromUsageInTemplate() {
    myFixture.enableInspections(TypeScriptValidateTypesInspection::class.java)
    myFixture.getAllQuickFixes("changeMethodSignature.html", "changeMethodSignature.ts", "package.json")
    val fixTitle = JavaScriptBundle.message("change.method.signature.fix.text", "HeroDetailComponent.save()")
    myFixture.launchAction(myFixture.findSingleIntention(fixTitle))
    myFixture.checkResultByFile("changeMethodSignature.ts", "changeMethodSignature.fixed.ts", true)
  }

  fun testOverriddenMethods() {
    myFixture.configureByFiles("overriddenMethods.ts", "package.json")
    myFixture.completeBasic()
    assertEquals(listOf("\$any%(arg: any)" + BaseJSCompletionTestCase.getLocationPresentation(null, "overriddenMethods.ts") + "#any",
                        "bar%()#string",
                        "bar%(test: boolean)#string",
                        "bar%(test: string)#string",
                        "foo%null#string"),
                 ContainerUtil.sorted(AngularTestUtil.renderLookupItems(myFixture, false, true, true, true)))
  }

  fun testNonNullAssertionResolutionTypeScript() {
    myFixture.configureByFiles("nonNullAssertion.html", "nonNullAssertion.ts", "package.json", "customer.ts", "customer2.ts")
    val resolve = resolveReference("ca<caret>ll()")
    assertEquals("customer.ts", resolve.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(resolve, TypeScriptFunction::class.java)
  }

  fun testES6Refactorings() {
    myFixture.configureByFiles("package.json")
    myFixture.configureByText("test.html", "{{ 'foo<caret>'}}")
    UsefulTestCase.assertEmpty(myFixture.filterAvailableIntentions("Replace with template string"))
  }

  fun testGenericParentClassMembers() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("genericParentClassMembers.html", "genericParentClassMembers.ts",
                               "ng_for_of.ts", "iterable_differs.ts", "package.json")
    myFixture.checkHighlighting()
    assertEquals("""
                   {
                     someName: string;
                     someValue: number;
                   }
                   """.trimIndent(),
                 AngularTestUtil.resolveReference("item.some<caret>Name", myFixture).getParent().getText())
  }

  fun testUnionsWithoutTypeGuardSupport() {
    TypeScriptTestUtil.forceConfig(project, null, testRootDisposable)
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection::class.java, JSUnresolvedReferenceInspection::class.java)
    myFixture.configureByFiles("unions.ts", "ng_for_of.ts", "iterable_differs.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testNgspEntityNoNg() {
    myFixture.enableInspections(CheckDtdReferencesInspection())
    myFixture.configureByFiles("ngsp-no-ng.html")
    myFixture.checkHighlighting()
  }

  fun testNgspEntityWithNg() {
    myFixture.enableInspections(CheckDtdReferencesInspection())
    myFixture.configureByFiles("ngsp-with-ng.html", "package.json")
    myFixture.checkHighlighting()
  }

  fun testNgspEntityCompletion() {
    myFixture.configureByFiles("package.json")
    myFixture.configureByText("foo.html", "&<caret>")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "ngsp")
  }
}
