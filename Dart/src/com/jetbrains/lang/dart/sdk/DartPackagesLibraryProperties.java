package com.jetbrains.lang.dart.sdk;

import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class DartPackagesLibraryProperties extends LibraryProperties<DartPackagesLibraryProperties> {

  private @NotNull Map<String, List<String>> myPackageNameToDirsMap;

  public DartPackagesLibraryProperties() {
    myPackageNameToDirsMap = new TreeMap<String, List<String>>();
  }

  @NotNull
  @MapAnnotation(surroundWithTag = false)
  public Map<String, List<String>> getPackageNameToDirsMap() {
    return myPackageNameToDirsMap;
  }

  public void setPackageNameToDirsMap(@NotNull final Map<String, List<String>> packageNameToDirsMap) {
    myPackageNameToDirsMap = packageNameToDirsMap;
  }

  public void setPackageNameToFileDirsMap(@NotNull final Map<String, List<File>> packageNameToFileDirsMap) {
    myPackageNameToDirsMap = new TreeMap<String, List<String>>();
    for (Map.Entry<String, List<File>> entry : packageNameToFileDirsMap.entrySet()) {
      List<String> stringList = myPackageNameToDirsMap.get(entry.getKey());
      if (stringList == null) {
        stringList = new ArrayList<String>();
        myPackageNameToDirsMap.put(entry.getKey(), stringList);
      }
      for (File file : entry.getValue()) {
        stringList.add(FileUtil.toSystemIndependentName(file.getPath()));
      }
    }
  }

  @Nullable
  public DartPackagesLibraryProperties getState() {
    return this;
  }

  public void loadState(final DartPackagesLibraryProperties state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public boolean equals(final Object obj) {
    return obj instanceof DartPackagesLibraryProperties &&
           myPackageNameToDirsMap.equals(((DartPackagesLibraryProperties)obj).getPackageNameToDirsMap());
  }

  public int hashCode() {
    return myPackageNameToDirsMap.hashCode();
  }
}
