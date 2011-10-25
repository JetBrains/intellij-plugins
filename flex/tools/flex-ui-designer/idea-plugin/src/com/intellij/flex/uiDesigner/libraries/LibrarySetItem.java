package com.intellij.flex.uiDesigner.libraries;

import gnu.trove.THashSet;
import gnu.trove.TLinkableAdaptor;

import java.util.Set;

public final class LibrarySetItem extends TLinkableAdaptor {
  int inDegree;
  int definitionCounter;

  public boolean filtered;

  private int overloadedDefinitionCount = -1;
  public final Library library;

  // filtered if:
  // 1) unresolved (has unresolved dependencies)
  // 2) we found another class def with greater timestamp
  final Set<CharSequence> filteredDefinitions = new THashSet<CharSequence>();
  public final Set<LibrarySetItem> successors = new THashSet<LibrarySetItem>();
  public final THashSet<LibrarySetItem> parents = new THashSet<LibrarySetItem>();

  LibrarySetItem(Library library) {
    this.library = library;
  }

  public boolean hasFilteredDefinitions() {
    return !filteredDefinitions.isEmpty();
  }

  boolean hasUnresolvedDefinitions() {
    return overloadedDefinitionCount != filteredDefinitions.size();
  }

  void finalizeProcessCatalog() {
    assert overloadedDefinitionCount == -1;
    overloadedDefinitionCount = filteredDefinitions.size();
  }

  boolean hasDefinitions() {
    return definitionCounter > 0;
  }

  @Override
  public String toString() {
    return library.getFile().getNameWithoutExtension();
  }
}
