package cucumber.examples.java.calculator;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs {
  @Then("^I wait for (\\.[\\d]+) seconds$")
  public void i_wait_for(int change) {}

  @Then("^I wait about (\\.[\\d]+) seconds$")
  public void i_wait_about(int change) {}
}
