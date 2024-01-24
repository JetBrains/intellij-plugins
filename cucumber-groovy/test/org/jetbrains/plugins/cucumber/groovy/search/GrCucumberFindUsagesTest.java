// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy.search;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageInfo;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberLightTestCase;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GrCucumberFindUsagesTest extends GrCucumberLightTestCase {

  protected void doTest(String text, String ext, String additional, String additionalExt, int count) {
    if (additional != null && additionalExt != null) {
      getFixture().addFileToProject("steps." + additionalExt, additional);
    }

    getFixture().configureByText("current." + ext, text);

    int flags = TargetElementUtil.ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED;
    final PsiElement targetElement = TargetElementUtil.findTargetElement(getFixture().getEditor(), flags);
    assertNotNull("Cannot find referenced element", targetElement);

    Collection<UsageInfo> usages = getFixture().findUsages(targetElement);
    assertEquals(count, usages.size());
  }

  protected void doTest(String text, String ext, String additional, String additionalExt) {
    doTest(text, ext, additional, additionalExt, 1);
  }

  protected void doTest(String text, String ext, String additional) {
    doTest(text, ext, additional, null, 1);
  }

  protected void doTest(String text, String ext) {
    doTest(text, ext, null, null, 1);
  }

  @Test
  public void step() {
    doTest("""
# language: en
Feature: Division
 In order to avoid silly mistakes
 Cashiers must be able to calculate a fraction

 Scenario: go all east
   Given a mower in 0,0 facing north
   When I piv<caret>ot the mower to the right
   And I move the mower
   And I move the mower
   And I move the mower
   And I move the mower
   And I move the mower
   And I move the mow
   And I move the mow1
   Then the mower should be in 5,0 facing east
""", "feature", """

this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

Given(~'^a mower in (\\\\d+),(\\\\d+) facing north$') { int arg1, int arg2 ->
   // Express the Regexp above with the code you wish you had
   throw new PendingException()
}
When(~'^I pivot the mower to the right$') {->
   // Express the Regexp above with the code you wish you had
   throw new PendingException()
}
""", "groovy");
  }

  @Test
  public void scenarioOutlineStep() {
    doTest("""
# language: en
Feature: Division
 In order to avoid silly mistakes
 Cashiers must be able to calculate a fraction

Scenario Outline: eating
   Given there a<caret>re <start> cucumbers
   When I eat <eat> cucumbers
   Then I should have <left> cucumbers

 Examples:
   | start | eat | left |
   | 12    | 5   | 7    |
   | 20    | 5   | 15   |
""", "feature", """

this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

Given(~'^there are (\\\\d+) cucumbers$') {int number ->
   throw new PendingException()
}
""", "groovy");
  }

  @Test
  public void stepDefinition() {
    doTest("""
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

Given(~'^there a<caret>re (\\\\d+) cucumbers$') {int number ->
   throw new PendingException()
}
""", "groovy", """
# language: en
Feature: Division
 In order to avoid silly mistakes
 Cashiers must be able to calculate a fraction

Scenario Outline: eating
   Given there are <start> cucumbers
   When I eat <eat> cucumbers
   Then I should have <left> cucumbers

 Examples:
   | start | eat | left |
   | 12    | 5   | 7    |
   | 20    | 5   | 15   |
""", "feature");
  }

  @Test
  public void stepDefinition2() {
    doTest("""
import cucumber.runtime.PendingException

this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

Given(~'^a mow<caret>er in (\\\\d+),(\\\\d+) facing north_java$') { int arg1, int arg2 ->
   // Express the Regexp above with the code you wish you had
   throw new PendingException()
}""", "groovy", """
# language: en
Feature: Division_other
 In order to avoid silly mistakes
 Cashiers must be able to calculate a fraction

 Scenario: go all east
   Given a mower in 0,0 facing north_java
   When I pivot the mower to the right_java
   Given a commit 'fix'_java
   Given a commit3 'fix'    _java
""", "feature");
  }
}
