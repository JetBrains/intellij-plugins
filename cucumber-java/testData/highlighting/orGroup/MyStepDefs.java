package cucumber.examples.java.calculator;

import io.cucumber.java.en.Given;

public class MyStepDefs {
  @Given("a/an {word} is good")
  public void isGood(String word) {
    System.out.println("Word: " + word);
  }
}
