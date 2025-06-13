package cucumber.examples.java.calculator;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ShoppingStepdefs {
  @Given("^the following groceries:$")
  public void the_following_groceries() {
  }

  public void <warning descr="Method 'my_change_should_be_(int)' is never used">my_change_should_be_</warning>(int <warning descr="Parameter 'change' is never used">change</warning>) {
  }

  @Given("^I am out of milk$") // Test for IDEA-371083
  public void <warning descr="Method 'out_of_milk()' is never used">out_of_milk</warning>() {
  }

  @Then("I am happy")
  public void i_am_happy() {
  }

  @When("I am never used")
  @Then("I am double annotated happy")
  public void i_am_happy_double_annotated() {
  }
}
