package com.jetbrains.lang.dart.ide.index;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface DartShowHideInfo {
  @NotNull
  Set<String> getShowComponents();

  @NotNull
  Set<String> getHideComponents();
}
