package com.intellij.flex.uiDesigner.libraries;

public final class LibrarySetItem {
  int definitionCounter;

  public final Library library;

  LibrarySetItem(Library library) {
    this.library = library;
  }

  boolean hasDefinitions() {
    return definitionCounter > 0;
  }

  @Override
  public String toString() {
    return library.getFile().getNameWithoutExtension();
  }
}
