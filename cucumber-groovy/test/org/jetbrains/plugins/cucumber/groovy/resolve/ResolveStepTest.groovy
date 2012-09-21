package org.jetbrains.plugins.cucumber.groovy.resolve

import org.jetbrains.plugins.cucumber.groovy.GrCucumberTestUtil
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex
import org.jetbrains.plugins.groovy.LightGroovyTestCase
/**
 * @author Max Medvedev
 */
class ResolveStepTest extends LightGroovyTestCase {
  final String basePath = GrCucumberTestUtil.testDataPath + 'resolve'

  void testTable() {
    doTest('''\
Feature: Shopping

  Scenario: Give correct change
    Given the following<caret> groceries:
      | name  | price |
      | milk  | 9     |
      | bread | 7     |
      | soap  | 5     |
    When I pay 25
    Then my change should be 4
''', '''\
import static org.junit.Assert.assertEquals;

Given(~'^the following groceries:$') {List<Grocery> groceries ->
  for (Grocery grocery : groceries) {
    calc.push(grocery.price);
    calc.push("+");
  }
}

When(~'^I pay (\\\\d+)$') {int amount ->
  calc.push(amount);
  calc.push("-");
}

Then(~'^my change should be (\\\\d+)$') {int change ->
  assertEquals(-calc.value().intValue(), change);
}

public static class Grocery {
  public String name;
  public int price;
}
''')
  }

  void testSimple() {
    doTest('''\
# language: en
Feature: Division
  In order to avoid silly mistakes
  Cashiers must be able to calculate a fraction

  Scenario: More numbers
    Given I have en<caret>tered 6 into the calculator
    And have entered 3 into the calculator
    When I press divide
    Then the stored result should be 2.0
''', '''\
package calc

import cucumber.runtime.PendingException

this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

class CustomWorld {
    String customMethod() {
        "foo"
    }
}

World {
    new CustomWorld()
}

Given(~'I have entered (\\\\d+) into (.*) calculator') { int number, String ignore ->
    calc.push number
}

Given(~'(\\\\d+) into the') {->
    throw new RuntimeException("should never get here since we're running with --guess")
}
}
''')
  }

  void doTest(String feature, String stepDef) {
    myFixture.with {
      CucumberStepsIndex.getInstance(project).reset();
      configureByText('test.feature', feature)
      addFileToProject('steps.groovy', stepDef)

      def ref = file.findReferenceAt(editor.caretModel.offset)
      assertNotNull(ref)
      def resolved = ref.resolve()
      assertNotNull(resolved)
    }
  }
}
