package cucumber.examples.java.calculator;

import io.cucumber.java8.En;
import io.cucumber.java.en.Given;
import io.cucumber.java.ParameterType;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs implements En {
  public ShoppingStepdefs() {
    Given("my java8 step", () -> System.out.println("step"));
  }

  @Given("my step definition")
  public void my_step_definition() {
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
