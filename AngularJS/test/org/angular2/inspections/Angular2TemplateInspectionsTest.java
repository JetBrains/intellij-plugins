// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
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
    doTest(1, "onc<caret>lick", "Add attribute value", Angular2EmptyEventBindingInspection.class,
           "empty-event-binding.html");
  }

  public void testEmptyEventBinding2() {
    doTest(2, "on<caret>tap", "Add attribute value", Angular2EmptyEventBindingInspection.class,
           "empty-event-binding.html");
  }

  public void testBindingToEvent1() {
    doTest(1, "[on<caret>foo]", "Bind to event (foo)", Angular2BindingToEventInspection.class,
           "binding-to-event.html", "component.ts");
  }

  public void testBindingToEvent2() {
    doTest(2, "[on<caret>foo]", "Remove '[onfoo]' attribute", Angular2BindingToEventInspection.class,
           "binding-to-event.html", "component.ts");
  }

  public void testBindingToEvent3() {
    doTest(3, "[attr.on<caret>Foo]", "Bind to event (Foo)", Angular2BindingToEventInspection.class,
           "binding-to-event.html", "component.ts");
  }

  public void testNonEmptyNgContent() {
    doTest(1, "ff<caret>f", "Remove content", Angular2NonEmptyNgContentInspection.class,
           "non-empty-ng-content.html");
  }

  public void testMultipleTemplateBindings() {
    doTest(1, "*some<caret>thing", "Remove '*something' attribute", Angular2MultipleTemplateBindingsInspection.class,
           "multiple-template-bindings.html");
  }

  public void testAnimationTriggerAssignment1() {
    doTest(1, "@trigger=\"<caret>foo", "Bind to property [@trigger]", Angular2AnimationTriggerAssignmentInspection.class,
           "animation-trigger-assignment.html");
  }

  public void testAnimationTriggerAssignment2() {
    doTest(2, "@trigger=\"<caret>foo", "Remove attribute value", Angular2AnimationTriggerAssignmentInspection.class,
           "animation-trigger-assignment.html");
  }

  public void testTemplateReferenceVariable() {
    doTest(1, "#a<caret>bc=\"foo\"", "Remove '#abc' attribute", Angular2TemplateReferenceVariableInspection.class,
           "template-reference-variable.html", "component.ts");
  }

  public void testTemplateReferenceVariableWithModule() {
    doTest(1, "#a<caret>bc=\"foo\"", "Remove '#abc' attribute", Angular2TemplateReferenceVariableInspection.class,
           "template-reference-variable-with-module.html", "component.ts", "template-reference-variable-module.ts");
  }

  public void testMatchingComponents() {
    doTest(Angular2MatchingComponentsInspection.class,
           "matching-components.html", "component.ts");
  }

  public void testMatchingComponentsWithModule() {
    doTest(Angular2MatchingComponentsInspection.class,
           "matching-components-with-module.html", "component.ts", "matching-components-module.ts");
  }

  public void testBindings() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    doTest(Angular2BindingsInspection.class,
           "bindings.html", "component.ts");
  }

  public void testBindingsWithModule() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    doTest(Angular2BindingsInspection.class,
           "bindings-with-module.html", "component.ts", "bindings-module.ts");
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
