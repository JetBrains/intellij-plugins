package cucumber.examples.java.calculator;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs {
  @When("^I have short step$")
  public void I_have_short_step() throws Throwable {
  }

  @When("^I have short step with extension$")
  public void I_have_short_step_with_extension(int arg1, int arg2) throws Throwable {
  }
}
