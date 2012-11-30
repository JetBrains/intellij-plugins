package org.jetbrains.plugins.cucumber.groovy.search

import com.intellij.codeInsight.TargetElementUtilBase
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageInfo
import org.jetbrains.plugins.cucumber.groovy.GrCucumberLightTestCase
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex
/**
 * @author Max Medvedev
 */
class GrCucumberFindUsagesTest extends GrCucumberLightTestCase {
  final String basePath = null

  protected void doTest(Map args) {
    CucumberStepsIndex.getInstance(project).reset()

    def features = args.feature
    if (features instanceof String) features = [features]

    def groovys = args.groovy
    if (groovys instanceof String) groovys = [groovys]

    def current = args.current
    assert current instanceof Map<String, String>

    def count = args.count

    groovys.eachWithIndex { String text, int i ->
      myFixture.addFileToProject("steps${i}.groovy", text)
    }

    features.eachWithIndex { String text, int i ->
      myFixture.addFileToProject("feature${i}.feature", text)
    }

    myFixture.configureByText("current.${current.ext}", current.text)

    def flags = TargetElementUtilBase.ELEMENT_NAME_ACCEPTED | TargetElementUtilBase.REFERENCED_ELEMENT_ACCEPTED
    final PsiElement targetElement = TargetElementUtilBase.findTargetElement(myFixture.editor, flags)
    assert targetElement != null : "Cannot find referenced element";

    Collection<UsageInfo> usages = myFixture.findUsages(targetElement)
    assertEquals(count, usages.size())
  }

  void testStep() {
    doTest(
            current: [ext:'feature', text:'''\
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
'''],
            groovy: '''
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
''',
            count: 1)
  }

  void testScenarioOutlineStep() {
    doTest(
            current: [ext:'feature', text:'''\
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
'''],
            groovy: '''
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

Given(~'^there are (\\\\d+) cucumbers$') {int number ->
    throw new PendingException()
}
''',
            count: 1)
  }

  void testStepDefinition() {
    doTest(
            current: [ext: 'groovy', text: '''\
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

Given(~'^there a<caret>re (\\\\d+) cucumbers$') {int number ->
    throw new PendingException()
}
'''],
            feature: '''\
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
''',
            count: 1
    )
  }


}
