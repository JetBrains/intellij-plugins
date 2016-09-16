package com.google.jstestdriver.idea.execution;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.testframework.sm.runner.TestProxyFilterProvider;
import com.intellij.javascript.testFramework.util.BrowserStacktraceFilters;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class JstdTestProxyFilterProvider implements TestProxyFilterProvider {

  private final Project myProject;

  public JstdTestProxyFilterProvider(@NotNull Project project) {
    myProject = project;
  }

  @Nullable
  @Override
  public Filter getFilter(@NotNull String nodeType, @NotNull String nodeName, @Nullable String nodeArguments) {
    if ("browser".equals(nodeType) && nodeArguments != null) {
      final File basePath = new File(nodeArguments);
      if (basePath.isAbsolute() && basePath.isDirectory()) {
        return BrowserStacktraceFilters.createFilter(nodeName, myProject, basePath.getAbsolutePath());
      }
    }
    if ("browserError".equals(nodeType) && nodeArguments != null) {
      File basePath = new File(nodeArguments);
      if (basePath.isDirectory() && basePath.isAbsolute()) {
        return new JsErrorFilter(myProject, basePath);
      }
    }
    return null;
  }
}
