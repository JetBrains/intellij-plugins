import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Steps {

  @When("Me be satisfied")
  @Then("Me be satisfied")
  public void i_am_happy() {
  }

  @When("I am dumb")
  public void i_am_dumb() {
  }

  @When("unrelated step")
  public void unrelated_step() {
  }
}
