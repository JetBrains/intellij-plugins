package com.sample;


import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MyStepDefs {
  @Given("^normal step$")
  public void normalStep() {
  }

  @Given("^failing step$")
  public void failingStep() {
    fail();
  }

  @Given("^error step$")
  public void errorStep() {
    throw new RuntimeException();
  }

  @Given("^pending step$")
  public void pendingStep() {
    throw new PendingException();
  }

  @Given("^step with parameter \"([^\"]*)\"$")
  public void stepWithParameter(String param) {
  }

  @Given("failing comparing step")
  public void failingComparingStep() {
    assertEquals("text", "act");
  }
}
