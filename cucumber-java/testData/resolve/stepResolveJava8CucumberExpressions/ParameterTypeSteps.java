package cucumber.examples.java.calculator;

import de.ppi.ServerAcceptanceTestSpringConfiguration;
import org.springframework.test.context.ContextConfiguration;

import cucumber.api.java8.En;

public class ParameterTypeSteps implements En {
  public ParameterTypeSteps() {
    testFeature1();
  }

  private void testFeature1() {
    Given("the string {string} is in the dummy repository",
               (String entry) -> sampleAcceptanceTest.givenRepositoryEntry(entry));

    When("this data is used in a method with parameter {string} \\(and we use an Ö)",
         (String parameter) -> sampleAcceptanceTest.whenMethodWithParameterCalled(parameter));

    Then("the result matches our expectation {string}", (String expectedResult)
      -> sampleAcceptanceTest.thenResultEqualsExpectedResult(expectedResult));
  }
}