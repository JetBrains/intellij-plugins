// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.AirSigningOptions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions.FilePathAndPathInPackage;

class AirPackagingOptionsBase {
  private boolean myEnabled = false;
  private boolean myUseGeneratedDescriptor = true;
  private final @NotNull List<FilePathAndPathInPackage> myFilesToPackage = new ArrayList<>();
  private @NotNull String myCustomDescriptorPath = "";
  private @NotNull String myPackageFileName = "";
  private @NotNull AirSigningOptions mySigningOptions = new AirSigningOptions();

  public boolean isEnabled() {
    return myEnabled;
  }

  public void setEnabled(final boolean enabled) {
    myEnabled = enabled;
  }

  public boolean isUseGeneratedDescriptor() {
    return myUseGeneratedDescriptor;
  }

  public void setUseGeneratedDescriptor(final boolean useGeneratedDescriptor) {
    myUseGeneratedDescriptor = useGeneratedDescriptor;
  }

  public @NotNull String getCustomDescriptorPath() {
    return myCustomDescriptorPath;
  }

  public void setCustomDescriptorPath(final @NotNull String customDescriptorPath) {
    myCustomDescriptorPath = customDescriptorPath;
  }

  public @NotNull String getPackageFileName() {
    return myPackageFileName;
  }

  public void setPackageFileName(final @NotNull String packageFileName) {
    myPackageFileName = packageFileName;
  }

  public @NotNull List<FilePathAndPathInPackage> getFilesToPackage() {
    return cloneList(myFilesToPackage);
  }

  public void setFilesToPackage(final @NotNull List<FilePathAndPathInPackage> filesToPackage) {
    myFilesToPackage.clear();
    for (FilePathAndPathInPackage filePathAndPathInPackage : filesToPackage) {
      myFilesToPackage.add(filePathAndPathInPackage.clone());
    }
  }

  public @NotNull AirSigningOptions getSigningOptions() {
    return mySigningOptions;
  }

  public void setSigningOptions(final @NotNull AirSigningOptions signingOptions) {
    mySigningOptions = signingOptions;
  }

  void applyTo(AirPackagingOptionsBase copy) {
    copy.myEnabled = myEnabled;
    copy.myUseGeneratedDescriptor = myUseGeneratedDescriptor;
    copy.myCustomDescriptorPath = myCustomDescriptorPath;
    copy.myPackageFileName = myPackageFileName;
    copy.setFilesToPackage(myFilesToPackage);
    copy.mySigningOptions = mySigningOptions.getCopy();
  }

  public boolean isEqual(AirPackagingOptionsBase copy) {
    if (copy.myEnabled != myEnabled) return false;
    if (copy.myUseGeneratedDescriptor != myUseGeneratedDescriptor) return false;
    if (!copy.myCustomDescriptorPath.equals(myCustomDescriptorPath)) return false;
    if (!copy.myPackageFileName.equals(myPackageFileName)) return false;
    if (!FlexUtils.equalLists(copy.myFilesToPackage, myFilesToPackage)) return false;
    if (!copy.mySigningOptions.equals(mySigningOptions)) return false;

    return true;
  }

  private static List<FilePathAndPathInPackage> cloneList(final List<FilePathAndPathInPackage> filesToPackage) {
    final List<FilePathAndPathInPackage> clonedList = new ArrayList<>();
    for (FilePathAndPathInPackage filePathAndPathInPackage : filesToPackage) {
      clonedList.add(filePathAndPathInPackage.clone());
    }
    return clonedList;
  }
}
