package cucumber.examples.java.calculator;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs {
  @Then("^I wait for (\\.[\\d]+) seconds$")
  public void i_wait_for(int change) {}

  @Then("^I wait about (\\.[\\d]+) seconds$")
  public void i_wait_about(int change) {}
}
