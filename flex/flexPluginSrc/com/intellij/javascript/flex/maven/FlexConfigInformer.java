package com.intellij.javascript.flex.maven;

import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.openapi.project.Project;
import org.jetbrains.idea.maven.project.MavenProject;

public interface FlexConfigInformer {
  void showFlexConfigWarningIfNeeded(Project project, MavenProject mavenProject, FlexFacet flexFacet);
}
