package com.google.jstestdriver.idea.assertFramework.jstd;

import com.intellij.javascript.testFramework.AbstractTestFileStructureBuilder;
import com.intellij.javascript.testFramework.JsTestFileStructureBuilderProvider;
import org.jetbrains.annotations.NotNull;

public class JstdTestFileStructureBuilderProvider implements JsTestFileStructureBuilderProvider {
  @NotNull
  @Override
  public AbstractTestFileStructureBuilder getTestFileStructureBuilder() {
    return JstdTestFileStructureBuilder.getInstance();
  }
}
