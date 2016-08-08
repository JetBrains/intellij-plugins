package com.jetbrains.lang.dart.ide.index;

import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DartFileIndexData {
  private List<String> myClassNames = new ArrayList<>();
  private List<DartImportOrExportInfo> myImportAndExportInfos = new ArrayList<>();
  private Map<String, DartComponentInfo> myComponentInfoMap = new THashMap<>();
  private String myLibraryName;
  private List<String> myPartUris = new ArrayList<>();
  private List<String> mySymbols = new ArrayList<>();
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

  @NotNull
  public String getLibraryName() {
    return myLibraryName;
  }

  public void setLibraryName(@NotNull String libraryName) {
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
