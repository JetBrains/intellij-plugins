// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptFieldImpl;
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptParameterImpl;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection;
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection;
import com.intellij.psi.PsiElement;
import com.intellij.xml.util.CheckDtdReferencesInspection;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.inspections.Angular2TemplateInspectionsProvider;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.lang.javascript.BaseJSCompletionTestCase.getLocationPresentation;
import static com.intellij.util.containers.ContainerUtil.sorted;
import static org.angularjs.AngularTestUtil.renderLookupItems;

public class ContextTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "context";
  }

  @NotNull
  private PsiElement resolveReference(@NotNull String signature) {
    return AngularTestUtil.resolveReference(signature, myFixture);
  }

  public void testInlineTemplateCompletion2TypeScript() {
    myFixture.testCompletion("component.ts", "component.after.ts", "package.json");
  }

  public void testInlineTemplateResolve2TypeScript() {
    myFixture.configureByFiles("component.after.ts", "package.json");
    PsiElement resolve = resolveReference("=\"onComple<caret>tedButton()");
    assertEquals("component.after.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, JSFunction.class);
  }

  public void testInlineTemplateMethodResolve2TypeScript() {
    myFixture.configureByFiles("templateMethod.ts", "package.json", "customer.ts", "customer2.ts");
    PsiElement resolve = resolveReference("ca<caret>ll()");
    assertEquals("customer.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, TypeScriptFunction.class);
  }

  public void testNonInlineTemplateCompletion2TypeScript() {
    myFixture.testCompletion(
      "template.completion.html", "template.html", "package.json",
      "template.completion.ts");
  }

  public void testNonInlineTemplateResolve2TypeScript() {
    myFixture.configureByFiles("template.html", "package.json", "template.ts");
    PsiElement resolve = resolveReference("myCu<caret>");
    assertEquals("template.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, TypeScriptFieldImpl.class);
  }

  public void testNonInlineTemplateUsage2TypeScript() {
    myFixture.enableInspections(JSUnusedLocalSymbolsInspection.class, JSUnusedGlobalSymbolsInspection.class);
    myFixture.configureByFiles("template.usage.ts", "template.usage.html", "package.json");
    myFixture.checkHighlighting();
  }

  public void testNonInlineTemplateMethodResolve2TypeScript() {
    myFixture.configureByFiles("templateMethod.html", "package.json", "templateMethod.ts", "customer.ts", "customer2.ts");
    PsiElement resolve = resolveReference("ca<caret>ll()");
    assertEquals("customer.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, TypeScriptFunction.class);
  }

  public void testNonInlineTemplateDefinitionResolve2TypeScript() {
    myFixture.configureByFiles("definition.html", "package.json", "definition.ts", "definition2.ts");
    PsiElement resolve = resolveReference("tit<caret>le");
    assertEquals("definition.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, TypeScriptFieldImpl.class);
  }

  public void testInlineTemplateDefinitionResolve2TypeScript() {
    myFixture.configureByFiles("definition.ts", "package.json", "definition2.ts");
    PsiElement resolve = resolveReference("tit<caret>le");
    assertEquals("definition.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, TypeScriptFieldImpl.class);
  }

  public void testNonInlineTemplatePropertyResolve2TypeScript() {
    myFixture.configureByFiles("definition2.html", "package.json", "definition2.ts");
    PsiElement resolve = resolveReference("check<caret>ed");
    assertEquals("definition2.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, TypeScriptFieldImpl.class);
  }

  public void testInlineTemplatePropertyResolve2TypeScript() {
    myFixture.configureByFiles("definition2.ts", "package.json");
    PsiElement resolve = resolveReference("check<caret>ed");
    assertEquals("definition2.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, TypeScriptFieldImpl.class);
  }

  public void testComponentFieldsFromConstructorResolve() {
    myFixture.configureByFiles("template.constr.html", "template.constr.ts", "package.json");
    PsiElement resolve = resolveReference("myCu<caret>stomer");
    assertEquals("template.constr.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, TypeScriptParameterImpl.class);
  }

  public void testInlineComponentFieldsFromConstructorCompletion() {
    myFixture.testCompletion(
      "template.constr.completion.ts", "template.constr.completion.after.ts",
      "package.json");
  }

  public void testInlineTemplateCreateFunction2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection.class);
    myFixture.getAllQuickFixes("createFunction.ts", "package.json");
    myFixture.launchAction(myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")));
    myFixture.checkResultByFile("createFunction.fixed.ts", true);
  }

  public void testInlineTemplateCreateFunctionWithParam2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection.class);
    myFixture.getAllQuickFixes("createFunctionWithParam.ts", "package.json");
    myFixture.launchAction(myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")));
    myFixture.checkResultByFile("createFunctionWithParam.fixed.ts", true);
  }

  public void testInlineTemplateCreateFunctionEventEmitter2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection.class);
    myFixture.getAllQuickFixes("createFunctionEventEmitter.ts", "package.json");
    myFixture.launchAction(myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")));
    myFixture.checkResultByFile("createFunctionEventEmitter.fixed.ts", true);
  }

  public void testInlineTemplateCreateFunctionWithType2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection.class);
    myFixture.getAllQuickFixes("createFunctionWithType.ts", "package.json");
    myFixture.launchAction(myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")));
    myFixture.checkResultByFile("createFunctionWithType.fixed.ts", true);
  }

  public void testInlineTemplateCreateFunctionEventEmitterImplicit2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection.class);
    myFixture.getAllQuickFixes("createFunctionEventEmitterImplicit.ts", "package.json");
    myFixture.launchAction(myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")));
    myFixture.checkResultByFile("createFunctionEventEmitterImplicit.fixed.ts", true);
  }

  public void testInlineTemplateCreateField2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection.class);
    myFixture.getAllQuickFixes("createField.ts", "package.json");
    myFixture.launchAction(myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.field.intention.name", "todo")));
    myFixture.checkResultByFile("createField.fixed.ts", true);
  }

  public void testNonInlineTemplateCreateFunction2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection.class);
    myFixture.getAllQuickFixes("createFunction.html", "createFunction.ts", "package.json");
    myFixture.launchAction(myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")));
    myFixture.checkResultByFile("createFunction.ts", "createFunction.fixed.ts", true);
  }

  public void testNonInlineTemplateCreateField2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection.class);
    myFixture.getAllQuickFixes("createField.html", "createField.ts", "package.json");
    myFixture.launchAction(myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.field.intention.name", "todo")));
    myFixture.checkResultByFile("createField.ts", "createField.fixed.ts", true);
  }

  public void testNonInlineTemplateCreateFunctionDoubleClass2TypeScript() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection.class);
    myFixture.getAllQuickFixes("createFunctionDoubleClass.html", "createFunctionDoubleClass.ts", "package.json");
    myFixture.launchAction(myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.method.intention.name", "fetchFromApi")));
    myFixture.checkResultByFile("createFunctionDoubleClass.ts", "createFunctionDoubleClass.fixed.ts", true);
  }

  public void testCreateFieldWithExplicitPublicModifier() {
    JSTestUtils.testWithTempCodeStyleSettings(getProject(), settings -> {
      settings.getCustomSettings(TypeScriptCodeStyleSettings.class).USE_PUBLIC_MODIFIER = true;
      myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection.class);
      myFixture.configureByFiles("createFieldWithExplicitPublic.html", "createFieldWithExplicitPublic.ts", "package.json");
      myFixture.launchAction(myFixture.findSingleIntention(JavaScriptBundle.message("javascript.create.field.intention.name", "unresolved")));
      myFixture.checkResultByFile("createFieldWithExplicitPublic.ts", "createFieldWithExplicitPublic.fixed.ts", true);
    });
  }

  public void testFixSignatureMismatchFromUsageInTemplate() {
    myFixture.enableInspections(TypeScriptValidateTypesInspection.class);
    myFixture.getAllQuickFixes("changeMethodSignature.html", "changeMethodSignature.ts", "package.json");
    String fixTitle = JavaScriptBundle.message("change.method.signature.fix.text", "HeroDetailComponent.save()");
    myFixture.launchAction(myFixture.findSingleIntention(fixTitle));
    myFixture.checkResultByFile("changeMethodSignature.ts", "changeMethodSignature.fixed.ts", true);
  }

  public void testOverriddenMethods() {
    myFixture.configureByFiles("overriddenMethods.ts", "package.json");
    myFixture.completeBasic();
    assertEquals(List.of("$any%(arg: any)" + getLocationPresentation(null, "overriddenMethods.ts") + "#any",
                         "bar%()#string",
                         "bar%(test: boolean)#string",
                         "bar%(test: string)#string",
                         "foo%null#string"),
                 sorted(renderLookupItems(myFixture, false, true, true, true)));
  }

  public void testNonNullAssertionResolutionTypeScript() {
    myFixture.configureByFiles("nonNullAssertion.html", "nonNullAssertion.ts", "package.json", "customer.ts", "customer2.ts");
    PsiElement resolve = resolveReference("ca<caret>ll()");
    assertEquals("customer.ts", resolve.getContainingFile().getName());
    assertInstanceOf(resolve, TypeScriptFunction.class);
  }

  public void testES6Refactorings() {
    myFixture.configureByFiles("package.json");
    myFixture.configureByText("test.html", "{{ 'foo<caret>'}}");
    assertEmpty(myFixture.filterAvailableIntentions("Replace with template string"));
  }

  public void testGenericParentClassMembers() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureByFiles("genericParentClassMembers.html", "genericParentClassMembers.ts",
                               "ng_for_of.ts", "iterable_differs.ts", "package.json");
    myFixture.checkHighlighting();
    assertEquals("""
                   {
                     someName: string;
                     someValue: number;
                   }""",
                 AngularTestUtil.resolveReference("item.some<caret>Name", myFixture).getParent().getText());
  }

  public void testUnionsWithoutTypeGuardSupport() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection.class);
    myFixture.configureByFiles("unions.ts", "ng_for_of.ts", "iterable_differs.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testNgspEntityNoNg() {
    myFixture.enableInspections(new CheckDtdReferencesInspection());
    myFixture.configureByFiles("ngsp-no-ng.html");
    myFixture.checkHighlighting();
  }

  public void testNgspEntityWithNg() {
    myFixture.enableInspections(new CheckDtdReferencesInspection());
    myFixture.configureByFiles("ngsp-with-ng.html", "package.json");
    myFixture.checkHighlighting();
  }

  public void testNgspEntityCompletion() {
    myFixture.configureByFiles("package.json");
    myFixture.configureByText("foo.html", "&<caret>");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "ngsp");
  }
}
