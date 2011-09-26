package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.jstestdriver.idea.assertFramework.JstdRunElement;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QUnitModuleStructure extends AbstractQUnitModuleStructure {

  private final String myName;
  private final JSCallExpression myEnclosingCallExpression;
  private final JSObjectLiteralExpression myLifecycleObjectLiteral;

  public QUnitModuleStructure(@NotNull QUnitFileStructure fileStructure,
                              @NotNull String name,
                              @NotNull JSCallExpression enclosingCallExpression,
                              @Nullable JSObjectLiteralExpression lifecycleObjectLiteral) {
    super(fileStructure, name);
    myName = name;
    myEnclosingCallExpression = enclosingCallExpression;
    myLifecycleObjectLiteral = lifecycleObjectLiteral;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public JSCallExpression getEnclosingCallExpression() {
    return myEnclosingCallExpression;
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

  @Override
  JstdRunElement findJstdRunElement(@NotNull TextRange textRange) {
    JstdRunElement jstdRunElement = super.findJstdRunElement(textRange);
    if (jstdRunElement == null) {
      if (myEnclosingCallExpression.getTextRange().contains(textRange)) {
        return JstdRunElement.newTestCaseRunElement(getName());
      }
    }
    return jstdRunElement;
  }
}
