package com.intellij.flex.uiDesigner;

public class EmbedLibrary implements Library {
  private String path;

  public EmbedLibrary(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  @Override
  public String toString() {
    return path;
  }
}
