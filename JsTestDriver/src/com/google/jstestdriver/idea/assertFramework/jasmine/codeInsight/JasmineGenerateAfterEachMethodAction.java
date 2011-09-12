package com.google.jstestdriver.idea.assertFramework.jasmine.codeInsight;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class JasmineGenerateAfterEachMethodAction extends JasmineGenerateMissingLifecycleMethodAction {
  @NotNull
  @Override
  public String getHumanReadableDescription() {
    return "Jasmine afterEach";
  }

  @Override
  public String getMethodName() {
    return "afterEach";
  }
}
