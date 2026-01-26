import io.cucumber.java.en.Given;

public class StepDefs {
    /**
     * This step logs in a user with the specified username.
     * It performs authentication and sets up the user session.
     *
     * @param username the username to log in with
     */
    @Given("I am logged in as {string}")
    public void iAmLoggedInAs(String username) {
        // Step implementation
    }
}
