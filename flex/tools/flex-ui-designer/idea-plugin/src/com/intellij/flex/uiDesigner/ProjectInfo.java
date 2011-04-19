package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.InfoList;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.openapi.project.Project;

public class ProjectInfo extends InfoList.Info<Project> {
  private final LibrarySet librarySet;

  public ProjectInfo(Project project, final LibrarySet librarySet) {
    super(project);
    this.librarySet = librarySet;
  }

  public LibrarySet getLibrarySet() {
    return librarySet;
  }
}
