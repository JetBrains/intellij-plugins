package com.google.jstestdriver.idea.assertFramework.qunit;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import org.jetbrains.annotations.NotNull;

public class QUnitTestMethodStructure {

  public static final String JSTD_NAME_PREFIX = "test ";

  private final AbstractQUnitModuleStructure myModuleStructure;
  private final String myName;
  private final JSCallExpression myJsCallExpression;
  private final JSFunctionExpression myFunctionExpression;

  public QUnitTestMethodStructure(@NotNull AbstractQUnitModuleStructure moduleStructure,
                                  @NotNull String name,
                                  @NotNull JSCallExpression jsCallExpression,
                                  @NotNull JSFunctionExpression jsFunctionExpression) {
    myModuleStructure = moduleStructure;
    myName = name;
    myJsCallExpression = jsCallExpression;
    myFunctionExpression = jsFunctionExpression;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public String getNameWithJstdPrefix() {
    return JSTD_NAME_PREFIX + myName;
  }

  @NotNull
  public JSCallExpression getCallExpression() {
    return myJsCallExpression;
  }

  @NotNull
  public AbstractQUnitModuleStructure getModuleStructure() {
    return myModuleStructure;
  }
}
