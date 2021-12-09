// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.index;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DartFileIndexData {
  private final List<String> myClassNames = new ArrayList<>();
  private final List<DartImportOrExportInfo> myImportAndExportInfos = new ArrayList<>();
  private final Map<String, DartComponentInfo> myComponentInfoMap = new HashMap<>();
  @Nullable private String myLibraryName;
  private final List<String> myPartUris = new ArrayList<>();
  private final List<String> mySymbols = new ArrayList<>();
  private boolean myIsPart;

  public List<String> getClassNames() {
    return myClassNames;
  }

  public void addClassName(@Nullable String name) {
    if (name != null) {
      myClassNames.add(name);
    }
  }

  public Map<String, DartComponentInfo> getComponentInfoMap() {
    return myComponentInfoMap;
  }

  public void addComponentInfo(@Nullable String name, DartComponentInfo info) {
    if (name != null) {
      myComponentInfoMap.put(name, info);
    }
  }

  public List<DartImportOrExportInfo> getImportAndExportInfos() {
    return myImportAndExportInfos;
  }

  public void addImportInfo(final @NotNull DartImportOrExportInfo importInfo) {
    myImportAndExportInfos.add(importInfo);
  }

  @Nullable
  public String getLibraryName() {
    return myLibraryName;
  }

  public void setLibraryName(@Nullable String libraryName) {
    myLibraryName = libraryName;
  }

  public List<String> getPartUris() {
    return myPartUris;
  }

  public void addPartUri(@NotNull final String partUri) {
    myPartUris.add(partUri);
  }

  public List<String> getSymbols() {
    return mySymbols;
  }

  public void addSymbol(@Nullable String name) {
    if (name != null) {
      mySymbols.add(name);
    }
  }

  public void setIsPart(final boolean isPart) {
    myIsPart = isPart;
  }

  public boolean isPart() {
    return myIsPart;
  }
}
