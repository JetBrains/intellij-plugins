import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Steps {

  @When("I am happy")
  public void i_am_<caret>happy() {
  }

  @When("I am angry")
  public void i_am_angry() {
  }

  @When("unrelated step")
  public void unrelated_step() {
  }
}
