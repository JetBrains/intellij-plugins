package com.jetbrains.lang.dart.util;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CaretPositionInfo {

  public final int caretOffset;
  @Nullable public final String expected;
  @Nullable public final List<String> completionEqualsList;
  @Nullable public final List<String> completionIncludesList;
  @Nullable public final List<String> completionExcludesList;

  public CaretPositionInfo(final int caretOffset,
                           @Nullable final String expected,
                           @Nullable final List<String> completionEqualsList,
                           @Nullable final List<String> completionIncludesList,
                           @Nullable final List<String> completionExcludesList) {
    this.caretOffset = caretOffset;
    this.expected = expected;
    this.completionEqualsList = completionEqualsList;
    this.completionIncludesList = completionIncludesList;
    this.completionExcludesList = completionExcludesList;
  }
}
