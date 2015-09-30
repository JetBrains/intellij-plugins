package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.execution.filters.ConsoleInputFilterProvider;
import com.intellij.execution.filters.InputFilter;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DartConsoleInputFilterProvider implements ConsoleInputFilterProvider {
  @NotNull
  public InputFilter[] getDefaultFilters(@NotNull Project project) {
    return new InputFilter[]{new DartConsoleInputFilter(project)};
  }
}
