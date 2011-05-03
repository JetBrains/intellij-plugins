package com.intellij.flex.uiDesigner.libraries;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LibrarySet {
  private final String id;
  private final LibrarySet parent;
  private final ApplicationDomainCreationPolicy applicationDomainCreationPolicy;
  private final List<Library> libraries;

  public LibrarySet(String id, ApplicationDomainCreationPolicy applicationDomainCreationPolicy, List<Library> libraries) {
    this(id, null, applicationDomainCreationPolicy, libraries);
  }

  public LibrarySet(String id, @Nullable LibrarySet parent, ApplicationDomainCreationPolicy applicationDomainCreationPolicy, List<Library> libraries) {
    this.id = id;
    this.parent = parent;

    this.applicationDomainCreationPolicy = applicationDomainCreationPolicy;
    this.libraries = libraries;
  }

  public String getId() {
    return id;
  }

  @Nullable
  public LibrarySet getParent() {
    return parent;
  }

  public List<Library> getLibraries() {
    return libraries;
  }

  public ApplicationDomainCreationPolicy getApplicationDomainCreationPolicy() {
    return applicationDomainCreationPolicy;
  }
}
