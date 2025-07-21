package cucumber.examples.java.calculator;

import io.cucumber.java.en.Given;

public class MyStepDefs {
  @Given("^step with quotes \"([^\"]*)\" and another quotes \"([^\"]*)\" on test$")
  public void step_with_quotes_and_another_quotes_on_test(String arg1, String arg2) {
  }

  @Given("^there (?:is|are) (\\d+) open sheets? for (.*) created by (.*)")
  public void there_open_sheets(int count, String project, String author) {
  }

  @Given("^I expect inspection warning on ([^>]+) with messages 1$")
  public void iExpectInspection1(String type) {
  }

  @Given("^I expect inspection warning on <([^>]+)> with messages 2$")
  public void iExpectInspection2(String type) {
  }

  @Given("^I expect inspection warning on <<([^>]+)>> with messages 3$")
  public void iExpectInspection3(String type) {
  }

  @Given("my another step definition with param {string}")
  public void my_another_step_definition(String param) {
  }
}
