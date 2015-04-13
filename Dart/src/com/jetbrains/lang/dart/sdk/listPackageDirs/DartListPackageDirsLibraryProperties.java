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

  private @NotNull Map<String, List<String>> myPackageNameToDirsMap;

  public DartListPackageDirsLibraryProperties() {
    myPackageNameToDirsMap = new TreeMap<String, List<String>>();
  }

  @NotNull
  @MapAnnotation(surroundWithTag = false, surroundKeyWithTag = false)
  public Map<String, List<String>> getPackageNameToDirsMap() {
    return myPackageNameToDirsMap;
  }

  public void setPackageNameToDirsMap(@NotNull final Map<String, List<String>> packageNameToDirsMap) {
    myPackageNameToDirsMap = packageNameToDirsMap;
  }

  public void setPackageNameToFileDirsMap(@NotNull final Map<String, List<String>> packageNameToFileDirsMap) {
    myPackageNameToDirsMap = new TreeMap<String, List<String>>();
    for (Map.Entry<String, List<String>> entry : packageNameToFileDirsMap.entrySet()) {
      List<String> stringList = myPackageNameToDirsMap.get(entry.getKey());
      if (stringList == null) {
        stringList = new ArrayList<String>();
        myPackageNameToDirsMap.put(entry.getKey(), stringList);
      }
      for (String s : entry.getValue()) {
        stringList.add(s);
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
