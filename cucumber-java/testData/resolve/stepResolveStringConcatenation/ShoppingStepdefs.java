package cucumber.examples.java.calculator;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs {
  @When("^I subtract (\\d+)" + " from (\\d+)$")
  public void I_subtract_from(int arg1, int arg2) throws Throwable {
  }

  @Then("^the result is (\\d+)$")
  public void the_result_is(double expected) {
  }
}
