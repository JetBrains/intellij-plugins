import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Steps {

  @And("I have {int} EUR on my account")
  public void i_have_EUR_on_my_account(int EUR) {
  }

  @When("unrelated step")
  public void unrelated_step() {
  }
}