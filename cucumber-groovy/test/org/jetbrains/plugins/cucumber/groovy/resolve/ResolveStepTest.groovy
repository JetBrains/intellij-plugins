// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.groovy.resolve

import com.intellij.psi.PsiReference
import groovy.transform.CompileStatic
import org.jetbrains.plugins.cucumber.groovy.GrCucumberLightTestCase
import org.jetbrains.plugins.groovy.util.ResolveTest
import org.junit.Test

@CompileStatic
class ResolveStepTest extends GrCucumberLightTestCase implements ResolveTest {

  @Test
  void table() {
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

this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

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

  @Test
  void simple() {
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

Given(~'^(\\\\d+) into the$') {->
    throw new RuntimeException("should never get here since we're running with --guess")
}
''')
  }

  @Test
  void methodWithTimeoutParameter() {
    doTest(
      '''
Feature: Division
  Scenario: More numbers
    Given calcula<caret>tor
''',
      '''
package calc

import cucumber.runtime.PendingException

this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

Given(~'calculator', 1000) {->
    calc.push number
}
'''
    )
  }

  void doTest(String feature, String stepDef) {
    fixture.configureByText('test.feature', feature)
    fixture.addFileToProject('steps.groovy', stepDef)
    assert referenceUnderCaret(PsiReference).resolve()
  }
}
