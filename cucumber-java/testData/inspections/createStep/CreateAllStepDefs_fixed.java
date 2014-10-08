package cucumber.examples.java.calculator;

import cucumber.annotation.en.Given;

public class CreateAllStepDefs {
    @Given("^step$")
    public void step(String arg1) throws Throwable {
    }

    @cucumber.api.java.en.Given("^test$")
    public void test() throws Throwable {
        // Express the Regexp above with the code you wish you had
        throw new cucumber.api.PendingException();
    }

    @cucumber.api.java.en.Given("^super test$")
    public void super_test() throws Throwable {
        // Express the Regexp above with the code you wish you had
        throw new cucumber.api.PendingException();
    }
}
