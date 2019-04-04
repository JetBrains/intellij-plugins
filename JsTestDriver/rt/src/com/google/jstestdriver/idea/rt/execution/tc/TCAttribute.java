package com.google.jstestdriver.idea.rt.execution.tc;

import org.jetbrains.annotations.NotNull;

/**
* @author Sergey Simonchik
*/
public enum TCAttribute {

  NAME("name"),
  NODE_ID("nodeId"),
  TEST_DURATION("duration"),
  LOCATION_URL("locationHint"),
  ACTUAL("actual"),
  EXPECTED("expected"),
  DETAILS("details"),
  STDOUT("out"),
  EXCEPTION_MESSAGE("message"),
  EXCEPTION_STACKTRACE("details"),
  IS_TEST_ERROR("error"),
  PARENT_NODE_ID("parentNodeId"),
  NODE_TYPE("nodeType"),
  NODE_ARGS("nodeArgs"),
  TEST_COUNT("count")
  ;

  private final String myName;

  TCAttribute(@NotNull String name) {
    myName = name;
  }

  @NotNull
  public String getName() {
    return myName;
  }
}
