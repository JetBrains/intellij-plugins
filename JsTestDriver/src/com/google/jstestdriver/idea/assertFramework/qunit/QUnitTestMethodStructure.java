package com.google.jstestdriver.idea.assertFramework.qunit;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import org.jetbrains.annotations.NotNull;

public class QUnitTestMethodStructure {

  private final String myName;
  private final JSCallExpression myJsCallExpression;
  private final JSFunctionExpression myFunctionExpression;

  public QUnitTestMethodStructure(@NotNull String name, @NotNull JSCallExpression jsCallExpression, @NotNull JSFunctionExpression jsFunctionExpression) {
    myName = name;
    myJsCallExpression = jsCallExpression;
    myFunctionExpression = jsFunctionExpression;
  }

  public String getName() {
    return myName;
  }

  public JSCallExpression getCallExpression() {
    return myJsCallExpression;
  }
}
