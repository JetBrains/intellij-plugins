package com.google.jstestdriver.idea.execution.settings.ui;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.project.Project;

class CreationContext {
  private final Project myProject;

  CreationContext(Project myProject) {
    this.myProject = myProject;
  }

  @NotNull
  public Project getProject() {
    return myProject;
  }

}
