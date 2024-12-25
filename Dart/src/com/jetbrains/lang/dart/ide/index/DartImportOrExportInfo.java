// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.index;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DartImportOrExportInfo implements DartShowHideInfo {
  public enum Kind {Import, Export}

  private final @NotNull Kind myKind;
  private final @NotNull String myUri;
  private final @Nullable String myImportPrefix;
  private final @NotNull Set<String> myShowComponents;
  private final @NotNull Set<String> myHideComponents;

  public DartImportOrExportInfo(final @NotNull Kind kind,
                                final @NotNull String uri,
                                final @Nullable String importPrefix,
                                final @NotNull Set<String> showComponents,
                                final @NotNull Set<String> hideComponents) {
    myKind = kind;
    myUri = uri;
    myImportPrefix = kind == Kind.Export ? null : importPrefix;
    myShowComponents = showComponents;
    myHideComponents = hideComponents;
  }

  public @NotNull String getUri() {
    return myUri;
  }

  public @NotNull Kind getKind() {
    return myKind;
  }

  public @Nullable String getImportPrefix() {
    return myImportPrefix;
  }

  @Override
  public @NotNull Set<String> getShowComponents() {
    return myShowComponents;
  }

  @Override
  public @NotNull Set<String> getHideComponents() {
    return myHideComponents;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DartImportOrExportInfo info = (DartImportOrExportInfo)o;

    if (myKind != info.myKind) return false;
    if (!myUri.equals(info.myUri)) return false;
    if (myImportPrefix != null ? !myImportPrefix.equals(info.myImportPrefix) : info.myImportPrefix != null) return false;
    if (!myShowComponents.equals(info.myShowComponents)) return false;
    if (!myHideComponents.equals(info.myHideComponents)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myKind.hashCode();
    result = 31 * result + myUri.hashCode();
    result = 31 * result + (myImportPrefix != null ? myImportPrefix.hashCode() : 0);
    result = 31 * result + myShowComponents.hashCode();
    result = 31 * result + myHideComponents.hashCode();
    return result;
  }
}
