package cucumber.examples.java.calculator;

import cucumber.api.java.en.Given;

public class MyStepDefs {
  @Given("^step with quotes \"([^\"]*)\" and another quotes \"([^\"]*)\" on test$")
  public void step_with_quotes_and_another_quotes_on_test(String arg1, String arg2) throws Throwable {
  }

  @Given("^there (?:is|are) (\\d+) open sheets? for (.*) created by (.*)")
  public void there_open_sheets(int count, String project, String author) throws Throwable {
  }
}
