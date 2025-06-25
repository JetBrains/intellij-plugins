package atm;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class WithdrawSteps {

  @Given("I have {int} EUR on my account")
  public void i_have_EUR_on_my_account(int EUR) {
  }

  @When("^I withdraw (-?\\d+) EUR$")
  public void withdraw_EUR(int EUR) {
  }

  @Then("I get {int} EUR from the ATM")
  public void i_get_EUR_from_the_ATM(int EUR) {
  }

  @Then("error message about the lack of money is displayed")
  public void error_message_about_the_lack_of_money_is_displayed() {
  }

  @Then("My account has {int} EUR left")
  public void my_account_has_EUR_left(int EUR) {
  }

  @Then("error message about incorrect amount is displayed")
  public void error_message_about_incorrect_amount_is_displayed() {
  }

  @When("I am dumb")
  @Then("I am happy")
  public void i_am_happy() {
  }

  @Then("^I am angry very$")
  public void i_am_angry() {
  }

  @Then("I have {int} feeling(s)")
  public void i_have_feelings(int a) {
  }

  @Then("I have no/few/many feeling(s) about this/that")
  public void i_have_feelings2() {
  }

  @Then("I have {int} {word} feeling(s) uh")
  public void i_have_feelings3(int a, String b) {
  }
}
