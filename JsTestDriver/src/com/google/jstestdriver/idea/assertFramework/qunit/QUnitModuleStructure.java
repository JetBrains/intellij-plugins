package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QUnitModuleStructure extends AbstractQUnitModuleStructure {

  private final String myName;
  private final JSCallExpression myJsCallExpression;
  private final JSObjectLiteralExpression myLifecycleObjectLiteral;

  public QUnitModuleStructure(@NotNull QUnitFileStructure fileStructure,
                              @NotNull String name,
                              @NotNull JSCallExpression jsCallExpression,
                              @Nullable JSObjectLiteralExpression lifecycleObjectLiteral) {
    super(fileStructure, name);
    myName = name;
    myJsCallExpression = jsCallExpression;
    myLifecycleObjectLiteral = lifecycleObjectLiteral;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public JSCallExpression getEnclosingCallExpression() {
    return myJsCallExpression;
  }

  @Nullable
  public JSProperty findLifecycleMethodByName(@NotNull String methodName) {
    if (myLifecycleObjectLiteral == null) {
      return null;
    }
    JSProperty[] properties = JsPsiUtils.getProperties(myLifecycleObjectLiteral);
    for (JSProperty property : properties) {
      String propertyName = JsPsiUtils.getPropertyName(property);
      if (methodName.equals(propertyName)) {
        return property;
      }
    }
    return null;
  }

  @Nullable
  public JSObjectLiteralExpression getLifecycleObjectLiteral() {
    return myLifecycleObjectLiteral;
  }
}
