package com.intellij.flex.uiDesigner;

public class ProjectInfo {
  private final LibrarySet librarySet;

  public ProjectInfo(final LibrarySet librarySet) {
    this.librarySet = librarySet;
  }

  public LibrarySet getLibrarySet() {
    return librarySet;
  }
}
