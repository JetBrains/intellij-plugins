package com.google.jstestdriver.idea.assertFramework.jasmine.codeInsight;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class JasmineGenerateBeforeEachMethodAction extends JasmineGenerateMissingLifecycleMethodAction {
  @NotNull
  @Override
  public String getHumanReadableDescription() {
    return "Jasmine beforeEach";
  }

  @Override
  public String getMethodName() {
    return "beforeEach";
  }
}
