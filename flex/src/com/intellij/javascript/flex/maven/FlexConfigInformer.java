package com.intellij.javascript.flex.maven;

import com.intellij.openapi.project.Project;

public interface FlexConfigInformer {
  void showFlexConfigWarningIfNeeded(Project project);
}
