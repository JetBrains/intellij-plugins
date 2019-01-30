// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2TemplateInspectionsTest extends LightPlatformCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "template";
  }

  public void testEmptyEventBinding1() {
    doTest(1, Angular2EmptyEventBindingInspection.class, "onc<caret>lick", "Add attribute value",
           "empty-event-binding.html");
  }

  public void testEmptyEventBinding2() {
    doTest(2, Angular2EmptyEventBindingInspection.class, "on<caret>tap", "Add attribute value",
           "empty-event-binding.html");
  }

  public void testBindingToEvent1() {
    doTest(1, Angular2BindingToEventInspection.class, "[on<caret>foo]", "Bind to event (foo)",
           "binding-to-event.html", "component.ts");
  }

  public void testBindingToEvent2() {
    doTest(2, Angular2BindingToEventInspection.class, "[on<caret>foo]", "Remove '[onfoo]' attribute",
           "binding-to-event.html", "component.ts");
  }

  public void testBindingToEvent3() {
    doTest(3, Angular2BindingToEventInspection.class, "[attr.on<caret>Foo]", "Bind to event (Foo)",
           "binding-to-event.html", "component.ts");
  }

  public void testNonEmptyNgContent() {
    doTest(1, Angular2NonEmptyNgContentInspection.class, "ff<caret>f", "Remove content",
           "non-empty-ng-content.html");
  }

  public void testMultipleTemplateBindings() {
    doTest(1, Angular2MultipleTemplateBindingsInspection.class, "*some<caret>thing", "Remove '*something' attribute",
           "multiple-template-bindings.html");
  }

  public void testAnimationTriggerAssignment1() {
    doTest(1, Angular2AnimationTriggerAssignmentInspection.class, "@trigger=\"<caret>foo", "Bind to property [@trigger]",
           "animation-trigger-assignment.html");
  }

  public void testAnimationTriggerAssignment2() {
    doTest(2, Angular2AnimationTriggerAssignmentInspection.class, "@trigger=\"<caret>foo", "Remove attribute value",
           "animation-trigger-assignment.html");
  }

  public void testTemplateReferenceVariable() {
    doTest(1, Angular2TemplateReferenceVariableInspection.class, "#a<caret>bc=\"foo\"", "Remove '#abc' attribute",
           "template-reference-variable.html", "component.ts");
  }

  public void testTemplateReferenceVariableWithModule() {
    doTest(1, Angular2TemplateReferenceVariableInspection.class, "#a<caret>bc=\"foo\"", "Remove '#abc' attribute",
           "template-reference-variable-with-module.html", "component.ts", "template-reference-variable-module.ts");
  }

  public void testMatchingComponents() {
    doTest(1, Angular2MatchingComponentsInspection.class, null, null,
           "matching-components.html", "component.ts");
  }

  public void testMatchingComponentsWithModule() {
    doTest(1, Angular2MatchingComponentsInspection.class, null, null,
           "matching-components-with-module.html", "component.ts", "matching-components-module.ts");
  }

  private void doTest(int testNr,
                      @NotNull Class<? extends LocalInspectionTool> inspection,
                      @Nullable String location,
                      @Nullable String quickFixName,
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
