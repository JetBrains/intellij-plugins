package cucumber.examples.java.calculator;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import cucumber.runtime.java.StepDefAnnotation;

import java.util.List;

@StepDefAnnotation
public class ShoppingStepdefs {

  @Given("^the following groceries:$")
  public void the_following_groceries(List<Grocery> groceries) {
  }

  @When("^I pay (\\d+)$")
  public void i_pay(int amount) {
  }

  @Then("^my change should be (\\d+)$")
  public void my_change_should_be_(int change) {
  }

  public static class Grocery {
    public String name;
    public int price;
  }
}
