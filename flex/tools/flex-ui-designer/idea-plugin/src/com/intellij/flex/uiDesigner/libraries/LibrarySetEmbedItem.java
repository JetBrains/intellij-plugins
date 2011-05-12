package com.intellij.flex.uiDesigner.libraries;

public class LibrarySetEmbedItem {
  public final String path;
  public final LibrarySetItem parent;

  public LibrarySetEmbedItem(String path, LibrarySetItem parent) {
    this.path = path;
    this.parent = parent;
  }

  @Override
  public String toString() {
    return path;
  }
}
