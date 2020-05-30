// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.groovy.search

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageInfo
import groovy.transform.CompileStatic
import org.jetbrains.plugins.cucumber.groovy.GrCucumberLightTestCase
import org.junit.Test

import static org.junit.Assert.assertEquals

@CompileStatic
class GrCucumberFindUsagesTest extends GrCucumberLightTestCase {

  protected void doTest(String text, String ext, String additional = null, String additionalExt = null, int count = 1) {
    if (additional && additionalExt) {
      fixture.addFileToProject("steps.$additionalExt", additional)
    }

    fixture.configureByText("current.$ext", text)

    def flags = TargetElementUtil.ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED
    final PsiElement targetElement = TargetElementUtil.findTargetElement(fixture.editor, flags)
    assert targetElement != null: "Cannot find referenced element"

    Collection<UsageInfo> usages = fixture.findUsages(targetElement)
    assertEquals(count, usages.size())
  }

  @Test
  void step() {
    doTest(
      '''\
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
''', 'feature',
      '''
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
''', 'groovy')
  }

  @Test
  void scenarioOutlineStep() {
    doTest('''\
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
''', 'feature',
           '''
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

Given(~'^there are (\\\\d+) cucumbers$') {int number ->
    throw new PendingException()
}
''', 'groovy')
  }

  @Test
  void stepDefinition() {
    doTest(
      '''\
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

Given(~'^there a<caret>re (\\\\d+) cucumbers$') {int number ->
    throw new PendingException()
}
''', 'groovy',
      '''\
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
''', 'feature')
  }

  @Test
  void stepDefinition2() {
    doTest('''\
import cucumber.runtime.PendingException

this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

Given(~'^a mow<caret>er in (\\\\d+),(\\\\d+) facing north_java$') { int arg1, int arg2 ->
    // Express the Regexp above with the code you wish you had
    throw new PendingException()
}''', 'groovy',
           '''\
# language: en
Feature: Division_other
  In order to avoid silly mistakes
  Cashiers must be able to calculate a fraction

  Scenario: go all east
    Given a mower in 0,0 facing north_java
    When I pivot the mower to the right_java
    Given a commit 'fix'_java
    Given a commit3 'fix'    _java
''', 'feature')
  }
}
