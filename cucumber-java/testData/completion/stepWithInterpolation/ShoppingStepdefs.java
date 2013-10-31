package cucumber.examples.java.calculator;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs {
  private RpnCalculator calc = new RpnCalculator();

  @Then("^tests #{object} exists?$")
  public void my_change_should_be_(int change) {
    assertEquals(-calc.value().intValue(), change);
  }

  public static class Grocery {
    public String name;
    public int price;
  }
}
