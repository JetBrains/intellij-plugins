package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.AirSigningOptions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions.FilePathAndPathInPackage;

class AirPackagingOptionsBase {
  private boolean myEnabled = false;
  private boolean myUseGeneratedDescriptor = true;
  @NotNull private String myCustomDescriptorPath = "";
  @NotNull private String myPackageFileName = "";
  @NotNull private final List<FilePathAndPathInPackage> myFilesToPackage = new ArrayList<FilePathAndPathInPackage>();
  @NotNull private AirSigningOptions mySigningOptions = new AirSigningOptions();

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

  @NotNull
  public String getCustomDescriptorPath() {
    return myCustomDescriptorPath;
  }

  public void setCustomDescriptorPath(@NotNull final String customDescriptorPath) {
    myCustomDescriptorPath = customDescriptorPath;
  }

  @NotNull
  public String getPackageFileName() {
    return myPackageFileName;
  }

  public void setPackageFileName(@NotNull final String packageFileName) {
    myPackageFileName = packageFileName;
  }

  @NotNull
  public List<FilePathAndPathInPackage> getFilesToPackage() {
    return cloneList(myFilesToPackage);
  }

  public void setFilesToPackage(@NotNull final List<FilePathAndPathInPackage> filesToPackage) {
    myFilesToPackage.clear();
    for (FilePathAndPathInPackage filePathAndPathInPackage : filesToPackage) {
      myFilesToPackage.add(filePathAndPathInPackage.clone());
    }
  }

  @NotNull
  public AirSigningOptions getSigningOptions() {
    return mySigningOptions;
  }

  public void setSigningOptions(@NotNull final AirSigningOptions signingOptions) {
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
    final List<FilePathAndPathInPackage> clonedList = new ArrayList<FilePathAndPathInPackage>();
    for (FilePathAndPathInPackage filePathAndPathInPackage : filesToPackage) {
      clonedList.add(filePathAndPathInPackage.clone());
    }
    return clonedList;
  }
}
