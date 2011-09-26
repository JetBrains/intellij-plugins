package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.common.collect.Lists;
import com.google.inject.internal.Maps;
import com.google.jstestdriver.idea.assertFramework.AbstractTestFileStructure;
import com.google.jstestdriver.idea.assertFramework.JstdRunElement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class JasmineFileStructure extends AbstractTestFileStructure {

  private final List<JasmineSuiteStructure> mySuiteStructures = Lists.newArrayList();
  private final Map<String, JasmineSuiteStructure> mySuiteMap = Maps.newHashMap();

  public JasmineFileStructure(@NotNull JSFile jsFile) {
    super(jsFile);
  }

  public void addDescribeStructure(JasmineSuiteStructure suiteStructure) {
    mySuiteStructures.add(suiteStructure);
    mySuiteMap.put(suiteStructure.getName(), suiteStructure);
  }

  @Nullable
  public JasmineSuiteStructure findTopLevelSuiteByName(String suiteName) {
    return mySuiteMap.get(suiteName);
  }

  public int getTopLevelSuiteCount() {
    return mySuiteStructures.size();
  }

  public boolean hasJasmineSymbols() {
    return getTopLevelSuiteCount() > 0;
  }

  @Nullable
  public JasmineSuiteStructure findLowestSuiteStructureContainingOffset(int offset) {
    for (JasmineSuiteStructure suiteStructure : mySuiteStructures) {
      JasmineSuiteStructure inner = suiteStructure.findLowestSuiteStructureContainingOffset(offset);
      if (inner != null) {
        return inner;
      }
    }
    return null;
  }

  @Nullable
  public JasmineSpecStructure findSpecContainingOffset(int offset) {
    for (JasmineSuiteStructure suiteStructure : mySuiteStructures) {
      JasmineSpecStructure specStructure = suiteStructure.findSpecContainingOffset(offset);
      if (specStructure != null) {
        return specStructure;
      }
    }
    return null;
  }

  @Override
  @Nullable
  public JstdRunElement findJstdRunElement(@NotNull TextRange textRange) {
    for (JasmineSuiteStructure suiteStructure : mySuiteStructures) {
      JstdRunElement jstdRunElement = suiteStructure.findJstdRunElement(textRange);
      if (jstdRunElement != null) {
        return jstdRunElement;
      }
    }
    return null;
  }
}
