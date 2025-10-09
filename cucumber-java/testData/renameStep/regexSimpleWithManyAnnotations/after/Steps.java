import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class Steps {

  @Given("^I am finding a bug$")
  @Then("^I am far away$")
  public void foo() {
  }

  @Given("^some other step$")
  public void some_other_step() {
  }
}