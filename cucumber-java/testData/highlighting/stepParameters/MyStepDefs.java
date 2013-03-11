package cucumber.examples.java.calculator;

import cucumber.annotation.en.Given;

public class MyStepDefs {
  @Given("^step with quotes \"([^\"]*)\" and another quotes \"([^\"]*)\" on test$")
  public void step_with_quotes_and_another_quotes_on_test(String arg1, String arg2) throws Throwable {
  }
}
