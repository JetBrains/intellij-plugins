package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.lang.javascript.psi.JSCallExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class JasmineSuiteStructure implements JasmineSuiteChild {

  private final String myName;
  private final JSCallExpression mySuiteCallExpression;
  private final List<JasmineSuiteStructure> mySuiteChildren = Lists.newArrayList();
  private final List<JasmineSpecStructure> mySpecChildren = Lists.newArrayList();
  private final Map<String, JasmineSuiteChild> myChildByNameMap = Maps.newLinkedHashMap();

  public JasmineSuiteStructure(@NotNull String name, @NotNull JSCallExpression suiteCallExpression) {
    myName = name;
    mySuiteCallExpression = suiteCallExpression;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public JSCallExpression getSuiteCallExpression() {
    return mySuiteCallExpression;
  }

  public void addChild(@NotNull JasmineSuiteChild child) {
    if (child instanceof JasmineSuiteStructure) {
      JasmineSuiteStructure suiteStructure = (JasmineSuiteStructure) child;
      myChildByNameMap.put(suiteStructure.getName(), suiteStructure);
      mySuiteChildren.add(suiteStructure);
    } else if (child instanceof JasmineSpecStructure) {
      JasmineSpecStructure specStructure = (JasmineSpecStructure) child;
      myChildByNameMap.put(specStructure.getName(), specStructure);
      mySpecChildren.add(specStructure);
    }
  }

  @Nullable
  public JasmineSuiteStructure getInnerSuiteByName(String suiteName) {
    JasmineSuiteChild child = myChildByNameMap.get(suiteName);
    if (child instanceof JasmineSuiteStructure) {
      return (JasmineSuiteStructure) child;
    }
    return null;
  }

  @Nullable
  public JasmineSpecStructure getInnerSpecByName(String specName) {
    JasmineSuiteChild child = myChildByNameMap.get(specName);
    if (child instanceof JasmineSpecStructure) {
      return (JasmineSpecStructure) child;
    }
    return null;
  }

  public int getSuiteChildrenCount() {
    return mySuiteChildren.size();
  }

  public int getSpecChildrenCount() {
    return mySpecChildren.size();
  }
}
