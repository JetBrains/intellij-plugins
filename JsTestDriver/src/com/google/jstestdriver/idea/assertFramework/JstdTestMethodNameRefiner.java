package com.google.jstestdriver.idea.assertFramework;

import com.intellij.javascript.testFramework.AbstractTestFileStructure;
import com.intellij.javascript.testFramework.qunit.QUnitFileStructure;
import com.intellij.javascript.testFramework.util.TestMethodNameRefiner;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class JstdTestMethodNameRefiner implements TestMethodNameRefiner {

  public static JstdTestMethodNameRefiner INSTANCE = new JstdTestMethodNameRefiner();

  private JstdTestMethodNameRefiner() {}

  @NotNull
  @Override
  public String refine(@NotNull AbstractTestFileStructure structure, @NotNull String testMethodName) {
    if (structure instanceof QUnitFileStructure) {
      return StringUtil.trimStart(testMethodName, "test ");
    }
    return testMethodName;
  }

}
