package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.abc.AbcFilter;
import gnu.trove.THashSet;
import gnu.trove.TLinkableAdaptor;

import java.util.Collection;
import java.util.Set;

public class FilteredLibrary extends TLinkableAdaptor implements Library {
  public int inDegree;
  public int definitionCounter;

  public boolean filtered;

  public int unresolvedDefinitionPolicy;
  public final OriginalLibrary originalLibrary;

  public final Set<CharSequence> unresolvedDefinitions = new THashSet<CharSequence>(AbcFilter.HASHING_STRATEGY);
  public final Set<FilteredLibrary> successors = new THashSet<FilteredLibrary>();
  public Collection<Library> parents = new THashSet<Library>();

  public FilteredLibrary(OriginalLibrary originalLibrary) {
    this.originalLibrary = originalLibrary;
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
    return originalLibrary.getFile().getNameWithoutExtension();
  }

  @Override
  public Collection<Library> getParents() {
    return parents;
  }
}
