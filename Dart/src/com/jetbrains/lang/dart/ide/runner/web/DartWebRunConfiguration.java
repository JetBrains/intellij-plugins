package com.jetbrains.lang.dart.ide.runner.web;

import com.intellij.javascript.debugger.execution.JavaScriptDebugConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DartWebRunConfiguration extends JavaScriptDebugConfiguration {
  public DartWebRunConfiguration(@NotNull final String name,
                                 @NotNull final Project project,
                                 @NotNull final DartWebRunConfigurationType configurationType) {
    super(project, configurationType.getConfigurationFactories()[0], name);
  }
}
