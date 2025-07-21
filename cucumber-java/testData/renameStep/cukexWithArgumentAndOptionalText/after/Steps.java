import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Steps {

  @Then("I really do have {int} feeling(s)")
  public void i_have_feelings(int a) {
  }

  @When("unrelated step")
  public void unrelated_step() {
  }
}