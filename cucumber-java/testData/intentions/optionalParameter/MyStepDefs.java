package cucumber.examples.java.calculator;

import cucumber.api.java.en.Given;

public class MyStepDefs {
  @Given("^the \"([^\"]*)\" field(?: within (.*))? should contain \"([^\"]*)\"$")
  public void step(String field, String parent, String value) throws Throwable {
  }
}
