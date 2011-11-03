package com.intellij.flex.uiDesigner.libraries;

import gnu.trove.THashSet;

public final class LibrarySetItem {
  int definitionCounter;

  public final Library library;

  public final THashSet<LibrarySetItem> parents = new THashSet<LibrarySetItem>();

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
