package com.google.jstestdriver.idea.assertFramework.qunit.codeInsight;

import org.jetbrains.annotations.NotNull;

public class QUnitGenerateTearDownAction extends AbstractQUnitGenerateMissingLifecycleMethodAction {
  @NotNull
  @Override
  public String getHumanReadableDescription() {
    return "QUnit TearDown";
  }

  @NotNull
  @Override
  public String getMethodName() {
    return "teardown";
  }
}
