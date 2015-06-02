package cucumber.examples.java.calculator;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs {
  private RpnCalculator calc = new RpnCalculator();

  @Then("^tests? project exists?(?: with (\\d+))?$")
  public void my_change_should_be_(int change) {
    assertEquals(-calc.value().intValue(), change);
  }

  public static class Grocery {
    public String name;
    public int price;
  }
}
