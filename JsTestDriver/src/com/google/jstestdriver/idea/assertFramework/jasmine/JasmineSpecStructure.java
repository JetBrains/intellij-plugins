package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.intellij.lang.javascript.psi.JSCallExpression;
import org.jetbrains.annotations.NotNull;

public class JasmineSpecStructure implements JasmineSuiteChild {

  private final String myName;
  private final JSCallExpression mySpecCallExpression;

  public JasmineSpecStructure(@NotNull String name,
                              @NotNull JSCallExpression specCallExpression) {
    myName = name;
    mySpecCallExpression = specCallExpression;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public JSCallExpression getEnclosingCallExpression() {
    return mySpecCallExpression;
  }
}
