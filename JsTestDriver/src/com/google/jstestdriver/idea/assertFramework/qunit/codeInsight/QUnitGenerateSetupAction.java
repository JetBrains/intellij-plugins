package com.google.jstestdriver.idea.assertFramework.qunit.codeInsight;

import org.jetbrains.annotations.NotNull;

public class QUnitGenerateSetupAction extends AbstractQUnitGenerateMissingLifecycleMethodAction {
  @NotNull
  @Override
  public String getHumanReadableDescription() {
    return "QUnit Setup";
  }

  @NotNull
  @Override
  public String getMethodName() {
    return "setup";
  }
}
