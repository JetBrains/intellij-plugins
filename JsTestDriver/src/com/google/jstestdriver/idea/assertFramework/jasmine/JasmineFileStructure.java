package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.common.collect.Lists;
import com.google.inject.internal.Maps;
import com.google.jstestdriver.idea.assertFramework.AbstractTestFileStructure;
import com.intellij.lang.javascript.psi.JSFile;
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
}
