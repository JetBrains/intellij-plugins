import cucumber.api.java.en.When;

public class Steps {
  @When("test should pass")
  public void test_should_pass(Integer nr) {
  }

  @When("I set number to (\\d{2})")
  public void I_set_number_to(Integer nr) {
  }
}
