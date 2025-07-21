import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Steps {

  @When("^I spend (-?\\d+) USD$")
  public void withdraw_EUR(int EUR) {
  }

  @When("^unrelated step$")
  public void unrelated_step() {
  }
}