package com.intellij.flex.uiDesigner;

import gnu.trove.THashSet;
import gnu.trove.TLinkableAdaptor;

import java.util.Set;

public class FilteredLibrary extends TLinkableAdaptor implements Library {
  public boolean filtered;

  public int inDegree;
  public int definitionCounter;

  public CharSequence mxCoreFlexModuleFactoryClassName;

  private final Set<CharSequence> unresolvedDefinitions = new THashSet<CharSequence>();
  private final Set<FilteredLibrary> successors = new THashSet<FilteredLibrary>();
  public Set<FilteredLibrary> parents = new THashSet<FilteredLibrary>(); // only for debug

  private final OriginalLibrary origin;

  public FilteredLibrary(OriginalLibrary origin) {
    this.origin = origin;
  }

  public OriginalLibrary getOrigin() {
    return origin;
  }

  public Set<CharSequence> getUnresolvedDefinitions() {
    return unresolvedDefinitions;
  }

  public boolean hasUnresolvedDefinitions() {
    return !unresolvedDefinitions.isEmpty();
  }

  public boolean hasDefinitions() {
    return definitionCounter > 0;
  }

  public Set<FilteredLibrary> getSuccessors() {
    return successors;
  }

  @Override
  public String toString() {
    return origin.getFile().getNameWithoutExtension();
  }
}
