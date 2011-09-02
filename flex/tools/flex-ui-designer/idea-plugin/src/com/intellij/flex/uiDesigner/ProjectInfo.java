package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.Info;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;

public class ProjectInfo extends Info<Project> {
  private final LibrarySet sdkLibrarySet;
  private LibrarySet librarySet;

  private final Sdk sdk;

  // Shares between modules, if module sdk equals project sdk (project sdk equals sdk of first registered module). Otherwise module will has own demandedAssetCounter and allocatedAssetCounter
  public AssetCounterInfo assetCounterInfo;

  public ProjectInfo(Project project, LibrarySet sdkLibrarySet, Sdk sdk) {
    super(project);
    this.sdkLibrarySet = sdkLibrarySet;
    this.sdk = sdk;
  }

  @NotNull
  public LibrarySet getSdkLibrarySet() {
    return sdkLibrarySet;
  }

  @NotNull
  public LibrarySet getLibrarySet() {
    return librarySet == null ? sdkLibrarySet : librarySet;
  }
  
  public void setLibrarySet(LibrarySet librarySet) {
    this.librarySet = librarySet;
  }

  public Sdk getSdk() {
    return sdk;
  }
}
