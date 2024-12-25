// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.model.lib;

import org.jetbrains.annotations.Nullable;

public class JpsFlexLibraryProperties {

  private final @Nullable String myLibraryId;

  public JpsFlexLibraryProperties(final JpsFlexLibraryProperties properties) {
    myLibraryId = properties.myLibraryId;
  }

  public JpsFlexLibraryProperties(final @Nullable String libraryId) {
    myLibraryId = libraryId;
  }

  public @Nullable String getLibraryId() {
    return myLibraryId;
  }
}
