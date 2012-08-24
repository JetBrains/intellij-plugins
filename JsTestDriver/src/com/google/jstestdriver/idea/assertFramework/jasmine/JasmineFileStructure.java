package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.common.collect.Lists;
import com.google.inject.internal.Maps;
import com.google.jstestdriver.idea.assertFramework.AbstractTestFileStructure;
import com.google.jstestdriver.idea.assertFramework.JstdRunElement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JasmineFileStructure extends AbstractTestFileStructure {

  private final List<JasmineSuiteStructure> mySuiteStructures = Lists.newArrayList();
  private final Map<String, JasmineSuiteStructure> mySuiteMap = Maps.newHashMap();

  public JasmineFileStructure(@NotNull JSFile jsFile) {
    super(jsFile);
  }

  @NotNull
  public List<JasmineSuiteStructure> getSuites() {
    return mySuiteStructures;
  }

  @Override
  public boolean isEmpty() {
    return mySuiteMap.isEmpty();
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

  @Override
  public PsiElement findPsiElement(@NotNull String testCaseName, @Nullable String testMethodName) {
    JasmineSuiteStructure suite = findSuite(testCaseName);
    if (suite == null) {
      return null;
    }
    if (testMethodName == null) {
      return suite.getEnclosingCallExpression();
    }
    JasmineSpecStructure spec = suite.getInnerSpecByName(testMethodName);
    if (spec != null) {
      return spec.getEnclosingCallExpression();
    }
    return null;
  }

  @Nullable
  private JasmineSuiteStructure findSuite(@NotNull String suiteName) {
    JasmineSuiteStructure suite = mySuiteMap.get(suiteName);
    if (suite != null) {
      return suite;
    }
    int ind = suiteName.lastIndexOf(' ');
    while (ind >= 0) {
      String prefix = suiteName.substring(0, ind);
      suite = mySuiteMap.get(prefix);
      if (suite != null) {
        String suffix = suiteName.substring(ind + 1);
        JasmineSuiteStructure res = suite.findSuite(suffix);
        if (res != null) {
          return res;
        }
      }
      ind = suiteName.lastIndexOf(' ', ind - 1);
    }
    return null;
  }

  @NotNull
  @Override
  public List<String> getTopLevelElements() {
    if (mySuiteStructures.isEmpty()) {
      return Collections.emptyList();
    }
    List<String> out = new ArrayList<String>(mySuiteStructures.size());
    for (JasmineSuiteStructure structure : mySuiteStructures) {
      out.add(structure.getName());
    }
    return out;
  }

  @NotNull
  @Override
  public List<String> getChildrenOf(@NotNull String topLevelElementName) {
    JasmineSuiteStructure suiteStructure = mySuiteMap.get(topLevelElementName);
    if (suiteStructure == null) {
      return Collections.emptyList();
    }
    List<String> out = new ArrayList<String>(suiteStructure.getSpecCount());
    for (JasmineSpecStructure specStructure : suiteStructure.getSpecs()) {
      out.add(specStructure.getName());
    }
    return out;
  }
}
