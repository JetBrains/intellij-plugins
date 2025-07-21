import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Steps {

  @Given("I possess the amount of {int} USD on my acc")
  public void i_have_EUR_on_my_account(int EUR) {
  }

  @When("unrelated step")
  public void unrelated_step() {
  }
}