package com.intellij.flex.uiDesigner.libraries;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LibrarySet {
  private final int id;
  private final LibrarySet parent;
  private final ApplicationDomainCreationPolicy applicationDomainCreationPolicy;

  private final List<LibrarySetItem> items;
  private final List<LibrarySetItem> resourceBundleOnlyItems;
  private final List<LibrarySetEmbedItem> embedItems;

  public LibrarySet(int id, @Nullable LibrarySet parent, ApplicationDomainCreationPolicy applicationDomainCreationPolicy, List<LibrarySetItem> items, List<LibrarySetItem> resourceBundleOnlyItems, List<LibrarySetEmbedItem> embedItems) {
    this.id = id;
    this.parent = parent;

    this.applicationDomainCreationPolicy = applicationDomainCreationPolicy;
    this.items = items;
    this.resourceBundleOnlyItems = resourceBundleOnlyItems == null ? Collections.<LibrarySetItem>emptyList() : resourceBundleOnlyItems;
    this.embedItems = embedItems == null ? Collections.<LibrarySetEmbedItem>emptyList() : embedItems;
  }

  public int getId() {
    return id;
  }

  @Nullable
  public LibrarySet getParent() {
    return parent;
  }

  public List<LibrarySetItem> getItems() {
    return items;
  }

  public List<LibrarySetItem> getResourceBundleOnlyItems() {
    return resourceBundleOnlyItems;
  }

  public List<LibrarySetEmbedItem> getEmbedItems() {
    return embedItems;
  }

  public ApplicationDomainCreationPolicy getApplicationDomainCreationPolicy() {
    return applicationDomainCreationPolicy;
  }
}
