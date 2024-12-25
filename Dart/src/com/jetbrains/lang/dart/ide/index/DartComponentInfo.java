// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.index;

import com.jetbrains.lang.dart.DartComponentType;
import org.jetbrains.annotations.Nullable;

public class DartComponentInfo {
  private final @Nullable DartComponentType myComponentType;
  private final @Nullable String myLibraryName;

  public DartComponentInfo(final @Nullable DartComponentType componentType, final @Nullable String libraryName) {
    myComponentType = componentType;
    myLibraryName = libraryName;
  }

  public @Nullable DartComponentType getComponentType() {
    return myComponentType;
  }

  public @Nullable String getLibraryName() {
    return myLibraryName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DartComponentInfo info = (DartComponentInfo)o;

    if (myComponentType != info.myComponentType) return false;
    if (myLibraryName != null ? !myLibraryName.equals(info.myLibraryName) : info.myLibraryName != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myComponentType != null ? myComponentType.hashCode() : 0;
    result = 31 * result + (myLibraryName != null ? myLibraryName.hashCode() : 0);
    return result;
  }
}
