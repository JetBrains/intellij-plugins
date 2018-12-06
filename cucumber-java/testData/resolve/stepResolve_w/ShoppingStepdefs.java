package cucumber.examples.java.calculator;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.java.en.And;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs {
  @When("^способ оплаты (\\w*)$")
  public void payment_mode(String payMethod) throws Throwable {
  }
  
  @When("^Case Sensitivity Check$")
  public void caseSensitivityCheck() throws Throwable {
  }
}
