package com.intellij.lang.javascript.flex;

import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class FlexStackTraceFilterProvider implements ConsoleFilterProvider{
  @NotNull
  public Filter[] getDefaultFilters(final @NotNull Project project) {
    return new Filter[]{new FlexStackTraceFilter(project)};
  }
}
