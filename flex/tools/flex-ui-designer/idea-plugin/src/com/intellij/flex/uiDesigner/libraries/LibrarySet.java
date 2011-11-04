package com.intellij.flex.uiDesigner.libraries;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LibrarySet {
  private final int id;
  private final LibrarySet parent;

  private final List<Library> items;
  private final List<Library> resourceBundleOnlyItems;

  public LibrarySet(int id, @Nullable LibrarySet parent, List<Library> items, List<Library> resourceBundleOnlyItems) {
    this.id = id;
    this.parent = parent;

    this.items = items;
    this.resourceBundleOnlyItems = resourceBundleOnlyItems == null ? Collections.<Library>emptyList() : resourceBundleOnlyItems;
  }

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

  public List<Library> getResourceLibrariesOnly() {
    return resourceBundleOnlyItems;
  }
}
