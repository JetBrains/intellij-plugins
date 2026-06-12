package cucumber.examples.java.calculator;

import io.cucumber.java8.En;
import io.cucumber.java.en.Given;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs implements En {
  public ShoppingStepdefs() {
    Given("my java8 step", () -> System.out.println("step"));
  }

  @Given("my step definition")
  public void my_step_definition() {
  }

  @Given("smth with {MyEnum}")
  public void smthWithMyEnum(MyEnum myEnum) {
  }
}
