package cucumber.examples.java.calculator;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.util.List;

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

  @Then("^my test step$")
  @Given("^my another test step$")
  public void my_test_step() {
  }

  @When("I set number to (\\d{2})")
  public void I_set_number_to(Integer nr) {
  }

  @When("I set value to (\\d{2,10})")
  public void I_set_value_to(Integer nr) {
  }

  @When("test should pass")
  public void test_should_pass(Integer nr) {
  }

  public static class Grocery {
    public String name;
    public int price;
  }
}
