import io.cucumber.java8.En;

public class Steps implements En {

  public Steps() {
    When("I am <caret>happy", () -> {
    });

    When("I am angry", () -> {
    });

    When("unrelated step", () -> {
    });
  }
}
