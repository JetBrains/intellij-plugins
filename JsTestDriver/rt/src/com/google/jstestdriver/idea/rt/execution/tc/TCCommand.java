package com.google.jstestdriver.idea.rt.execution.tc;

import org.jetbrains.annotations.NotNull;

/**
* @author Sergey Simonchik
*/
public enum TCCommand {
  TEST_SUITE_STARTED("testSuiteStarted"),
  TEST_SUITE_FINISHED("testSuiteFinished"),
  TEST_STARTED("testStarted"),
  TEST_FINISHED("testFinished"),
  TEST_STDOUT("testStdOut"),
  TEST_STDERR("testStdErr"),
  TEST_FAILED("testFailed"),
  TEST_COUNT("testCount")
  ;

  private final String myName;

  TCCommand(@NotNull String name) {
    myName = name;
  }

  @NotNull
  public String getName() {
    return myName;
  }
}
