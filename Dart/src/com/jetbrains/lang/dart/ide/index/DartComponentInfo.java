package com.jetbrains.lang.dart.ide.index;

import com.jetbrains.lang.dart.DartComponentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public class DartComponentInfo {
  @NotNull private final String value;
  @Nullable private final DartComponentType type;
  @Nullable private final String libraryId;

  public DartComponentInfo(@NotNull String value, @Nullable DartComponentType type, @Nullable String libraryId) {
    this.value = value;
    this.type = type;
    this.libraryId = libraryId;
  }

  @NotNull
  public String getValue() {
    return value;
  }

  @Nullable
  public DartComponentType getType() {
    return type;
  }

  @Nullable
  public String getLibraryId() {
    return libraryId;
  }
}
