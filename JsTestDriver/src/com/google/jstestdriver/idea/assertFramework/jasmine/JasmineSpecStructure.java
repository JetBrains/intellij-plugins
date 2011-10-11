package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import org.jetbrains.annotations.NotNull;

public class JasmineSpecStructure implements JasmineSuiteChild {

  private final String myName;
  private final JSCallExpression mySpecCallExpression;
  private final JSFunctionExpression mySpecBody;

  public JasmineSpecStructure(@NotNull String name,
                              @NotNull JSCallExpression specCallExpression,
                              @NotNull JSFunctionExpression specBody) {
    myName = name;
    mySpecCallExpression = specCallExpression;
    mySpecBody = specBody;
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
