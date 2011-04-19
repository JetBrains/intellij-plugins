package com.intellij.flex.uiDesigner.libraries;

import java.util.List;

public class LibrarySet {
  private final String id;
  private final ApplicationDomainCreationPolicy applicationDomainCreationPolicy;
  private final List<Library> libraries;

  public LibrarySet(String id, ApplicationDomainCreationPolicy applicationDomainCreationPolicy, List<Library> libraries) {
    this.id = id;

    this.applicationDomainCreationPolicy = applicationDomainCreationPolicy;
    this.libraries = libraries;
  }

  public String getId() {
    return id;
  }

  public List<Library> getLibraries() {
    return libraries;
  }

  public ApplicationDomainCreationPolicy getApplicationDomainCreationPolicy() {
    return applicationDomainCreationPolicy;
  }
}
