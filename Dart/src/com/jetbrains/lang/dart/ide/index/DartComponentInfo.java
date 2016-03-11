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
