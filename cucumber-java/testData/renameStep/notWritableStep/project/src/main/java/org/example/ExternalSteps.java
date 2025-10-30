package org.example;

import io.cucumber.java.en.Given;

public class ExternalSteps {

  @Given("this step is defined in some external library")
  public void normal(String size) {
  }
}
