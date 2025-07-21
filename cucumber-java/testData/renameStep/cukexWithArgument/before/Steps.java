import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Steps {

  @Given("I have {int} EUR on my account")
  public void i_have_EUR_on_my_account(int EUR) {
  }

  @When("unrelated step")
  public void unrelated_step() {
  }
}