package cucumber.examples.java.calculator;

import cucumber.api.java.en.Given;

public class MyStepDefs {
  @Given("^I am on (.*)$")
  public void iAmOn(String p) throws Throwable {
  }

  @Given("^I should see (.*)$")
  public void iShouldSee(String p) throws Throwable {
  }

  @Given("^I follow \"([^\"]*)\"$")
  public void iFollow(String p) throws Throwable {
  }

  @Given("^I should see (.*) within \"([^\"]*)\"$")
  public void iShouldSeeWithin(String p) throws Throwable {
  }
}
