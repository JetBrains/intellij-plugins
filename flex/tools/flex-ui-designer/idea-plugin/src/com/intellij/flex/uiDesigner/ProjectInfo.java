package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.InfoList;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;

public class ProjectInfo extends InfoList.Info<Project> {
  private final LibrarySet librarySet;
  private final Sdk sdk;

  public ProjectInfo(Project project, LibrarySet librarySet, Sdk sdk) {
    super(project);
    this.librarySet = librarySet;
    this.sdk = sdk;
  }

  public LibrarySet getLibrarySet() {
    return librarySet;
  }

  public Sdk getSdk() {
    return sdk;
  }
}
