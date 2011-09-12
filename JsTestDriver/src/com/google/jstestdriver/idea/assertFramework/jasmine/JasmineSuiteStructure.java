package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class JasmineSuiteStructure implements JasmineSuiteChild {

  private final String myName;
  private final JSCallExpression myEnclosingCallExpression;
  private final JSFunctionExpression mySpecDefinitions;
  private final List<JasmineSuiteStructure> mySuiteChildren = Lists.newArrayList();
  private final List<JasmineSpecStructure> mySpecChildren = Lists.newArrayList();
  private final Map<String, JasmineSuiteChild> myChildByNameMap = Maps.newLinkedHashMap();

  public JasmineSuiteStructure(@NotNull String name,
                               @NotNull JSCallExpression enclosingCallExpression,
                               @NotNull JSFunctionExpression specDefinitions) {
    myName = name;
    myEnclosingCallExpression = enclosingCallExpression;
    mySpecDefinitions = specDefinitions;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public JSCallExpression getEnclosingCallExpression() {
    return myEnclosingCallExpression;
  }

  @NotNull
  public JSFunctionExpression getSpecDefinitions() {
    return mySpecDefinitions;
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

  @Nullable
  public JasmineSpecStructure findSpecContainingOffset(int offset) {
    for (JasmineSpecStructure specChild : mySpecChildren) {
      TextRange specTextRange = specChild.getEnclosingCallExpression().getTextRange();
      if (JsPsiUtils.containsOffsetStrictly(specTextRange, offset)) {
        return specChild;
      }
    }
    for (JasmineSuiteStructure suiteChild : mySuiteChildren) {
      JasmineSpecStructure specStructure = suiteChild.findSpecContainingOffset(offset);
      if (specStructure != null) {
        return specStructure;
      }
    }
    return null;
  }

  @Nullable
  public JasmineSuiteStructure findLowestSuiteStructureContainingOffset(int offset) {
    for (JasmineSuiteStructure suiteStructure : mySuiteChildren) {
      JasmineSuiteStructure inner = suiteStructure.findLowestSuiteStructureContainingOffset(offset);
      if (inner != null) {
        return null;
      }
    }
    TextRange suiteTextRange = myEnclosingCallExpression.getTextRange();
    if (JsPsiUtils.containsOffsetStrictly(suiteTextRange, offset)) {
      return this;
    }
    return null;
  }

}
