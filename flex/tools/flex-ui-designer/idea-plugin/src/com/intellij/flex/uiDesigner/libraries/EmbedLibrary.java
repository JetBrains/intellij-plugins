package com.intellij.flex.uiDesigner.libraries;

import java.util.Set;

public class EmbedLibrary implements Library {
  private final String path;
  public final Library parent;

  public EmbedLibrary(String path, Library parent) {
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

  @Override
  public boolean hasDefinitions() {
    return true;
  }

  @Override
  public Set<Library> getParents() {
    throw new UnsupportedOperationException();
  }
}
