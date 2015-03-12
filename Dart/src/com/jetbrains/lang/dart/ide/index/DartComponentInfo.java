package com.jetbrains.lang.dart.ide.index;

import com.jetbrains.lang.dart.DartComponentType;
import org.jetbrains.annotations.Nullable;

public class DartComponentInfo {
  @Nullable private final DartComponentType myComponentType;
  @Nullable private final String myLibraryName;

  public DartComponentInfo(@Nullable final DartComponentType componentType, @Nullable final String libraryName) {
    myComponentType = componentType;
    myLibraryName = libraryName;
  }

  @Nullable
  public DartComponentType getComponentType() {
    return myComponentType;
  }

  @Nullable
  public String getLibraryName() {
    return myLibraryName;
  }
}
