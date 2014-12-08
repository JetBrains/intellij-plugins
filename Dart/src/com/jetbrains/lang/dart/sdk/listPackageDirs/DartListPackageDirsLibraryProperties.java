package com.jetbrains.lang.dart.sdk.listPackageDirs;

import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class DartListPackageDirsLibraryProperties extends LibraryProperties<DartListPackageDirsLibraryProperties> {

  private @NotNull Map<String, Set<String>> myPackageNameToDirsMap;

  public DartListPackageDirsLibraryProperties() {
    myPackageNameToDirsMap = new TreeMap<String, Set<String>>();
  }

  @NotNull
  @MapAnnotation(surroundWithTag = false, surroundKeyWithTag = false)
  public Map<String, Set<String>> getPackageNameToDirsMap() {
    return myPackageNameToDirsMap;
  }

  public void setPackageNameToDirsMap(@NotNull final Map<String, Set<String>> packageNameToDirsMap) {
    myPackageNameToDirsMap = packageNameToDirsMap;
  }

  public void setPackageNameToFileDirsMap(@NotNull final Map<String, List<File>> packageNameToFileDirsMap) {
    myPackageNameToDirsMap = new TreeMap<String, Set<String>>();
    for (Map.Entry<String, List<File>> entry : packageNameToFileDirsMap.entrySet()) {
      Set<String> stringSet = myPackageNameToDirsMap.get(entry.getKey());
      if (stringSet == null) {
        stringSet = new TreeSet<String>();
        myPackageNameToDirsMap.put(entry.getKey(), stringSet);
      }
      for (File file : entry.getValue()) {
        stringSet.add(FileUtil.toSystemIndependentName(file.getPath()));
      }
    }
  }

  @Nullable
  public DartListPackageDirsLibraryProperties getState() {
    return this;
  }

  public void loadState(final DartListPackageDirsLibraryProperties state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public boolean equals(final Object obj) {
    return obj instanceof DartListPackageDirsLibraryProperties &&
           myPackageNameToDirsMap.equals(((DartListPackageDirsLibraryProperties)obj).getPackageNameToDirsMap());
  }

  public int hashCode() {
    return myPackageNameToDirsMap.hashCode();
  }
}
