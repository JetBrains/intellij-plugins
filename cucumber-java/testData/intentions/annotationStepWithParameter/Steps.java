package en;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.ParameterType;
import io.cucumber.java8.En;

public class Steps implements En {

    @Given("I am hungry")
    public void iAmHungry() {}

    @When("I ask for advice")
    public void iAskForAdvice() {}

    @When("I eat a {word} cookie")
    public void iEatCookie(String color) {
    }
}
