package com.google.jstestdriver.idea.assertFramework.jstd.codeInsight;

import org.jetbrains.annotations.NotNull;

public class JstdGenerateSetupAction extends AbstractJstdCreateStaticMethodAction {
  @NotNull
  @Override
  public String getHumanReadableDescription() {
    return "JsTestDriver Setup";
  }

  @NotNull
  @Override
  public String getMethodName() {
    return "setUp";
  }
}
