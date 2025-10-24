package example;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Steps {

  @And("the HAPPY mood is chosen")
  public void theHappyMoodIsChosen() {
  }

  @Given("there are {int} cucumbers OOPS UNRESOLVED")
  public void thereAreCucumbers(int count) {
  }

  @When("I eat {int} cucumbers")
  public void iEatCucumbers(int eaten) {
  }

  @Then("I should have {int} cucumbers")
  public void iShouldHaveCucumbers(int expected) {
  }
}
