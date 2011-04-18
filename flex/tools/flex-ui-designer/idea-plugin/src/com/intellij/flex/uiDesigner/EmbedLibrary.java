package com.intellij.flex.uiDesigner;

class EmbedLibrary implements Library {
  private final String path;
  public final OriginalLibrary parent;

  public EmbedLibrary(String path, OriginalLibrary parent) {
    this.path = path;
    this.parent = parent;
  }

  public String getPath() {
    return path;
  }

  @Override
  public String toString() {
    return path;
  }
}
