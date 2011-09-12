package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFunctionExpression;

public class JasmineSpecStructure implements JasmineSuiteChild {

  private final String myName;
  private final JSCallExpression mySpecCallExpression;
  private final JSFunctionExpression mySpecBody;

  public JasmineSpecStructure(String name, JSCallExpression specCallExpression, JSFunctionExpression specBody) {
    myName = name;
    mySpecCallExpression = specCallExpression;
    mySpecBody = specBody;
  }

  public String getName() {
    return myName;
  }

  public JSCallExpression getEnclosingCallExpression() {
    return mySpecCallExpression;
  }
}
