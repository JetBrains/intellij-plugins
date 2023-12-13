package com.jetbrains.lang.dart.ide.runner;

import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class DartConsoleFilterProvider implements ConsoleFilterProvider {
  @Override
  public Filter @NotNull [] getDefaultFilters(final @NotNull Project project) {
    return new Filter[]{new DartConsoleFilter(project)};
  }
}
