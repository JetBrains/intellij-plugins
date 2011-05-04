package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.InfoList;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;

public class ProjectInfo extends InfoList.Info<Project> {
  private final LibrarySet sdkLibrarySet;
  private LibrarySet librarySet;

  private final Sdk sdk;

  public ProjectInfo(Project project, LibrarySet sdkLibrarySet, Sdk sdk) {
    super(project);
    this.sdkLibrarySet = sdkLibrarySet;
    this.sdk = sdk;
  }

  public LibrarySet getSdkLibrarySet() {
    return sdkLibrarySet;
  }

  public LibrarySet getLibrarySet() {
    return librarySet;
  }
  
  public void setLibrarySet(LibrarySet librarySet) {
    this.librarySet = librarySet;
  }

  public Sdk getSdk() {
    return sdk;
  }
}
