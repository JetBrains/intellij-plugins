package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.abc.AbcFilter;
import gnu.trove.THashSet;
import gnu.trove.TLinkableAdaptor;

import java.util.Set;

public class LibrarySetItem extends TLinkableAdaptor {
  public int inDegree;
  public int definitionCounter;

  public boolean filtered;

  public int unresolvedDefinitionPolicy;
  public final Library library;

  public final Set<CharSequence> unresolvedDefinitions = new THashSet<CharSequence>(AbcFilter.HASHING_STRATEGY);
  public final Set<LibrarySetItem> successors = new THashSet<LibrarySetItem>();
  public final THashSet<LibrarySetItem> parents = new THashSet<LibrarySetItem>();

  public LibrarySetItem(Library library) {
    this.library = library;
  }

  public boolean hasUnresolvedDefinitions() {
    return !unresolvedDefinitions.isEmpty();
  }

  public boolean hasMissedDefinitions() {
    return unresolvedDefinitions.size() != unresolvedDefinitionPolicy;
  }

  public boolean hasDefinitions() {
    return definitionCounter > 0;
  }

  @Override
  public String toString() {
    return library.getFile().getNameWithoutExtension();
  }
}
