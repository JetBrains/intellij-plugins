// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.typescript.inspection.TypeScriptExplicitMemberTypeInspection;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.codeInsight.InspectionsTest;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @see InspectionsTest
 * @see Angular2DecoratorInspectionsTest
 */
public class Angular2TemplateInspectionsTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "template";
  }

  public void testEmptyEventBinding1() {
    doTest(1, "onc<caret>lick", "Add attribute value", AngularMissingEventHandlerInspection.class,
           "empty-event-binding.html");
  }

  public void testEmptyEventBinding2() {
    doTest(2, "on<caret>tap", "Add attribute value", AngularMissingEventHandlerInspection.class,
           "empty-event-binding.html");
  }

  public void testBindingToEvent1() {
    doTest(1, "[on<caret>foo]", "Bind to event (foo)", AngularInsecureBindingToEventInspection.class,
           "binding-to-event.html", "component.ts");
  }

  public void testBindingToEvent2() {
    doTest(2, "[on<caret>foo]", "Remove attribute [onfoo]", AngularInsecureBindingToEventInspection.class,
           "binding-to-event.html", "component.ts");
  }

  public void testBindingToEvent3() {
    doTest(3, "[attr.on<caret>Foo]", "Bind to event (Foo)", AngularInsecureBindingToEventInspection.class,
           "binding-to-event.html", "component.ts");
  }

  public void testNonEmptyNgContent() {
    doTest(1, "ff<caret>f", "Remove content", AngularNonEmptyNgContentInspection.class,
           "non-empty-ng-content.html");
  }

  public void testMultipleTemplateBindings() {
    doTest(1, "*some<caret>thing", "Remove attribute *something", AngularMultipleStructuralDirectivesInspection.class,
           "multiple-template-bindings.html");
  }

  public void testAnimationTriggerAssignment1() {
    doTest(1, "@trigger=\"<caret>foo", "Bind to property [@trigger]", AngularInvalidAnimationTriggerAssignmentInspection.class,
           "animation-trigger-assignment.html");
  }

  public void testAnimationTriggerAssignment2() {
    doTest(2, "@trigger=\"<caret>foo", "Remove attribute value", AngularInvalidAnimationTriggerAssignmentInspection.class,
           "animation-trigger-assignment.html");
  }

  public void testTemplateReferenceVariable() {
    doTest(1, "#a<caret>bc=\"foo\"", "Remove attribute #abc", AngularInvalidTemplateReferenceVariableInspection.class,
           "template-reference-variable.html", "component.ts");
  }

  public void testTemplateReferenceVariableWithModule() {
    doTest(1, "#a<caret>bc=\"foo\"", "Remove attribute #abc", AngularInvalidTemplateReferenceVariableInspection.class,
           "template-reference-variable-with-module.html", "component.ts", "template-reference-variable-module.ts", "forms.d.ts");
  }

  public void testMatchingComponents() {
    doTest(AngularAmbiguousComponentTagInspection.class,
           "matching-components.html", "component.ts");
  }

  public void testMatchingComponentsWithModule() {
    doTest(AngularAmbiguousComponentTagInspection.class,
           "matching-components-with-module.html", "component.ts", "matching-components-module.ts");
  }

  public void testBindings() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    doTest(AngularUndefinedBindingInspection.class,
           "bindings.html", "component.ts");
  }

  public void testBindingsWithModule() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    doTest(AngularUndefinedBindingInspection.class,
           "bindings-with-module.html", "component.ts", "bindings-module.ts");
  }

  public void testTags() {
    myFixture.enableInspections(HtmlUnknownTagInspection.class);
    doTest(AngularUndefinedTagInspection.class,
           "tags.html", "component.ts");
  }

  public void testTagsWithModule() {
    myFixture.enableInspections(HtmlUnknownTagInspection.class);
    doTest(AngularUndefinedTagInspection.class,
           "tags-with-module.html", "component.ts", "tags-module.ts");
  }

  public void testStandaloneDeclarables() {
    myFixture.enableInspections(HtmlUnknownTagInspection.class);
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    myFixture.enableInspections(AngularUndefinedTagInspection.class);
    myFixture.enableInspections(AngularUndefinedBindingInspection.class);
    doTest(TypeScriptUnresolvedReferenceInspection.class,
           "standalone-declarables.html", "standalone-declarables.ts", "component.ts");
  }

  public void testStandaloneDeclarablesInClassic() {
    myFixture.enableInspections(HtmlUnknownTagInspection.class);
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    myFixture.enableInspections(AngularUndefinedTagInspection.class);
    myFixture.enableInspections(AngularUndefinedBindingInspection.class);
    doTest(TypeScriptUnresolvedReferenceInspection.class,
           "standalone-declarables-in-classic.html", "standalone-declarables-in-classic.ts", "component.ts");
  }

  public void testNgContentSelector() {
    doTest(AngularInvalidSelectorInspection.class,
           "ng-content-selector.html");
  }

  public void testI18n1() {
    doTest(1, "i18n<caret>-\n", "Rename attribute to 'i18n-bar'", AngularInvalidI18nAttributeInspection.class,
           "i18n.html");
  }

  public void testI18n2() {
    doTest(2, "i18n-<caret>boo", "Create 'boo' attribute", AngularInvalidI18nAttributeInspection.class,
           "i18n.html");
  }

  public void testI18n3() {
    doTest(3, "i18n-<caret>b:boo", "Rename attribute to 'i18n-c:boo'", AngularInvalidI18nAttributeInspection.class,
           "i18n.html");
  }

  public void testHammerJS() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    doTest(AngularUndefinedBindingInspection.class, "hammerJs.html");
  }

  public void testTypeScriptSpecifyTypeNoFix() {
    doTestNoFix("no-specify-type-variable.html",
                TypeScriptExplicitMemberTypeInspection.class,
                JavaScriptBundle.message("typescript.specify.type.explicitly"));
  }

  public void testTypeScriptSpecifyTypeNoFixNgFor() {
    doTestNoFix("no-specify-type-variable-ng-for.html",
                TypeScriptExplicitMemberTypeInspection.class,
                JavaScriptBundle.message("typescript.specify.type.explicitly"));
  }

  public void testTypeScriptNoIntroduceVariable() {
    doTestNoFix("no-introduce-variable.html",
                null,
                JavaScriptBundle.message("javascript.introduce.variable.title.local"));
  }

  private void doTestNoFix(@NotNull String location,
                           @Nullable Class<? extends LocalInspectionTool> inspection,
                           @NotNull String quickFixName) {
    if (inspection != null) {
      myFixture.enableInspections(inspection);
    }
    myFixture.configureByFiles("package.json");
    myFixture.configureByFiles(location);
    myFixture.checkHighlighting();
    assertNull(myFixture.getAvailableIntention(quickFixName));
  }


  private void doTest(@NotNull Class<? extends LocalInspectionTool> inspection,
                      String... files) {
    doTest(1, null, null, inspection, files);
  }

  private void doTest(int testNr,
                      @Nullable String location,
                      @Nullable String quickFixName,
                      @NotNull Class<? extends LocalInspectionTool> inspection,
                      String... files) {
    myFixture.enableInspections(inspection);
    myFixture.configureByFiles("package.json");
    myFixture.configureByFiles(files);
    myFixture.checkHighlighting();
    if (location == null || quickFixName == null) {
      return;
    }
    AngularTestUtil.moveToOffsetBySignature(location, myFixture);
    myFixture.launchAction(myFixture.findSingleIntention(quickFixName));
    int lastDot = files[0].lastIndexOf('.');
    myFixture.checkResultByFile(files[0].substring(0, lastDot) + ".after" + testNr + files[0].substring(lastDot));
  }
}
