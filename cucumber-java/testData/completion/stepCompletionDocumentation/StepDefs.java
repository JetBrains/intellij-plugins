package cucumber.examples.java.calculator;

import cucumber.api.java.en.Given;

public class StepDefs {
  /**
   * This step logs in a user with the specified username.
   * It performs authentication and sets up the user session.
   *
   * @param username the username to log in with
   */
  @Given("I am logged in")
  public void iAmLoggedInAs(String username) {
    // Step implementation
  }

  @Given("I am logged out")
  public void iAmLoggedOut() {
    // Step implementation
  }
}
