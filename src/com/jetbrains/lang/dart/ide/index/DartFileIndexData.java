package com.jetbrains.lang.dart.ide.index;

import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DartFileIndexData {
  private List<String> myClassNames = new ArrayList<String>();
  private List<DartPathInfo> myImportPaths = new ArrayList<DartPathInfo>();
  private Map<String, DartComponentInfo> myComponentInfoMap = new THashMap<String, DartComponentInfo>();
  final Map<String, List<DartComponentInfo>> myInheritorsMap = new THashMap<String, List<DartComponentInfo>>();
  @Nullable private String myLibraryName;
  private List<String> myPaths = new ArrayList<String>();
  private List<String> mySymbols = new ArrayList<String>();

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

  public List<DartPathInfo> getImportPaths() {
    return myImportPaths;
  }

  public void addImport(@NotNull DartPathInfo pathInfo) {
    myImportPaths.add(pathInfo);
  }

  public Map<String, List<DartComponentInfo>> getInheritorsMap() {
    return myInheritorsMap;
  }

  public void addInheritor(@NotNull String className, DartComponentInfo inheritor) {
    List<DartComponentInfo> list = myInheritorsMap.get(className);
    if (list == null) {
      list = new ArrayList<DartComponentInfo>();
      myInheritorsMap.put(className, list);
    }
    list.add(inheritor);
  }

  @Nullable
  public String getLibraryName() {
    return myLibraryName;
  }

  public void setLibraryName(@Nullable String libraryName) {
    myLibraryName = libraryName;
  }

  public List<String> getPaths() {
    return myPaths;
  }

  public void addPath(String path) {
    myPaths.add(path);
  }

  public List<String> getSymbols() {
    return mySymbols;
  }

  public void addSymbol(@Nullable String name) {
    if (name != null) {
      mySymbols.add(name);
    }
  }
}
