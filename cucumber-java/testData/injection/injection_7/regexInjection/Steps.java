import io.cucumber.java.en.When;

public class Steps {
  @When("I ask for {what}")
  public void iAskForAdvice(String what) {
  }

  @When("^I pay (\\d+)$")
  public void i_pay(int amount) {
  }
}
