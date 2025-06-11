package cucumber.examples.java.calculator;

import cucumber.api.java8.En;

public class ParameterTypeSteps implements En {
  public ParameterTypeSteps() {
    testFeature1();
  }

  private void testFeature1() {
    Given("the string {string} is in the dummy repository", (String entry) -> {
    });

    When("this data is used in a method with parameter {string} \\(and we use an Ö)", (String parameter) -> {
    });

    Then("the result matches our expectation {string}", (String expectedResult) -> {
    });

    Then("the list should contain exactly {int} entries", TargetStatusSteps::testMethod);
  }
}