package cucumber.examples.java.calculator;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import cucumber.annotation.en.And;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs {
  @When("^способ оплаты (\\w*)$")
  public void payment_mode(String payMethod) throws Throwable {
  }
}
