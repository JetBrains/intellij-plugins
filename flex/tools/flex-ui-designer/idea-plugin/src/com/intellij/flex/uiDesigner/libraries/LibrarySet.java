package com.intellij.flex.uiDesigner.libraries;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.io.Identifiable;

import java.util.List;

public class LibrarySet implements Identifiable {
  private final int id;
  private final LibrarySet parent;

  private final List<Library> items;

  public LibrarySet(int id, @Nullable LibrarySet parent, List<Library> items) {
    this.id = id;
    this.parent = parent;

    this.items = items;
  }

  @Override
  public int getId() {
    return id;
  }

  @Nullable
  public LibrarySet getParent() {
    return parent;
  }

  public List<Library> getLibraries() {
    return items;
  }
}
