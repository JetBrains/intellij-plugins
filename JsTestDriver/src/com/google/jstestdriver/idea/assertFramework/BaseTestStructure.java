package com.google.jstestdriver.idea.assertFramework;

public class BaseTestStructure {

  private final String myTestName;

  public BaseTestStructure(String testName) {
    myTestName = testName;
  }

  public String getTestName() {
    return myTestName;
  }

}
