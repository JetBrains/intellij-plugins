package cucumber.examples.java.calculator;

import io.cucumber.java8.En;
import io.cucumber.java8.StepDefinitionBody;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.ParameterType;
import java.util.List;

public class ShoppingStepdefs implements En {
  public ShoppingStepdefs() {
    Given("my java8 step", () -> System.out.println("step"));

    And("my java8 step with cast", (StepDefinitionBody.A0) () -> {});

    When("^my \\\\ step java 8$", () -> {});
  }

  @When("^my \\\\ step java ann$")
  public void my_step_java_ann() {}

  @Given("my step definition")
  public void my_step_definition() {
  }
  
  @Given("my another step definition with param {string}")
  public void my_another_step_definition(String param) {
  }

  @Given("^I expect inspection warning on ([^>]+) with messages 1$")
  public void iExpectInspection1(String type) {
  }

  @Given("^I expect inspection warning on <([^>]+)> with messages 2$")
  public void iExpectInspection2(String type) {
  }

  @Given("^I expect inspection warning on <<([^>]+)>> with messages 3$")
  public void iExpectInspection3(String type) {
  }

  @Given("^step (red|black):$")
  public void my_step_with_colon(String param) {
  }

  @Given("first regex")
  @Given("second regex")
  public void my_double_definition() {
  }

  @Given("step {color}")
  public void colorParameterType() {}

  @ParameterType("red|blue|yellow")
  public String color(String color) {
    return "Text with color: " + color;
  }
}
