package steps;

import io.cucumber.java.en.Given;

public class Steps {

  @Given("the step is {word}")
  public void test_1a(String path) {
  }

  @Given("^the step is (.+)$")
  public void test_1b(String path) {
  }

  @Given("another step is {word} blah!")
  public void test_2a(String path) {
  }

  @Given("another step is {word} blah!")
  public void test_2b(String path) {
  }

  @Given("another step is {word} blah!")
  public void test_2c(String path) {
  }

  @Given("this step is very unambiguous")
  public void unambiguousStep() {
  }

  // The 2 examples below are from IDEA-99170

  @Given("^(current)? branch ([^ ]+)$")
  public void test_a(String path) {
  }

  @And("^(current )?branch (\\S+)(?: in '(.+)')?$")
  public void test_b(String path) {
  }
}
