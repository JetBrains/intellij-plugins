package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.common.collect.Lists;
import com.google.inject.internal.Maps;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class QUnitModuleStructure {

  private final boolean myIsDefault;
  private final String myName;
  private final JSCallExpression myJsCallExpression;
  private List<QUnitTestMethodStructure> myTestMethodStructures = Lists.newArrayList();
  private Map<String, QUnitTestMethodStructure> myNameMap = Maps.newHashMap();

  private QUnitModuleStructure(boolean isDefault, @NotNull String name, @Nullable JSCallExpression jsCallExpression) {
    myIsDefault = isDefault;
    myName = name;
    myJsCallExpression = jsCallExpression;
  }

  public void addTestMethodStructure(QUnitTestMethodStructure qUnitTestMethodStructure) {
    myTestMethodStructures.add(qUnitTestMethodStructure);
    myNameMap.put(qUnitTestMethodStructure.getName(), qUnitTestMethodStructure);
  }

  public String getName() {
    return myName;
  }

  public JSCallExpression getJsCallExpression() {
    return myJsCallExpression;
  }

  public QUnitTestMethodStructure getTestMethodStructureByName(String testMethodName) {
    return myNameMap.get(testMethodName);
  }

  public boolean isDefault() {
    return myIsDefault;
  }

  public static QUnitModuleStructure newDefaultModule() {
    return new QUnitModuleStructure(true, "Default", null);
  }

  public static QUnitModuleStructure newRegularModule(@NotNull String name, @NotNull JSCallExpression jsCallExpression) {
    return new QUnitModuleStructure(false, name, jsCallExpression);
  }
}
