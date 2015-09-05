package com.intellij.flex.model.lib;

import org.jetbrains.annotations.Nullable;

public class JpsFlexLibraryProperties {

  private @Nullable String myLibraryId;

  public JpsFlexLibraryProperties(final JpsFlexLibraryProperties properties) {
    myLibraryId = properties.myLibraryId;
  }

  public JpsFlexLibraryProperties(final @Nullable String libraryId) {
    myLibraryId = libraryId;
  }

  @Nullable
  public String getLibraryId() {
    return myLibraryId;
  }
}
