package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.AirSigningOptions;
import com.intellij.lang.javascript.flex.actions.airinstaller.AirInstallerParametersBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class AirPackagingOptionsBase {
  private boolean myEnabled = false;
  private boolean myUseGeneratedDescriptor = true;
  @NotNull private String myApplicationId = "";
  @NotNull private String myCustomDescriptorPath = "";
  @NotNull private String myPackageFileName = "";
  @NotNull private final List<AirInstallerParametersBase.FilePathAndPathInPackage> myFilesToPackage =
    new ArrayList<AirInstallerParametersBase.FilePathAndPathInPackage>();
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
  public String getApplicationId() {
    return myApplicationId;
  }

  public void setApplicationId(@NotNull final String applicationId) {
    myApplicationId = applicationId;
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
  public List<AirInstallerParametersBase.FilePathAndPathInPackage> getFilesToPackage() {
    return myFilesToPackage;
  }

  public void setFilesToPackage(@NotNull final List<AirInstallerParametersBase.FilePathAndPathInPackage> filesToPackage) {
    myFilesToPackage.clear();
    myFilesToPackage.addAll(filesToPackage);
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
    copy.myApplicationId = myApplicationId;
    copy.myCustomDescriptorPath = myCustomDescriptorPath;
    copy.myPackageFileName = myPackageFileName;
    copy.myFilesToPackage.clear();
    copy.myFilesToPackage.addAll(AirInstallerParametersBase.cloneList(myFilesToPackage));
    copy.mySigningOptions = mySigningOptions.getCopy();
  }

  public boolean isEqual(AirPackagingOptionsBase copy) {
    if (copy.myEnabled != myEnabled) return false;
    if (copy.myUseGeneratedDescriptor != myUseGeneratedDescriptor) return false;
    if (!copy.myApplicationId.equals(myApplicationId)) return false;
    if (!copy.myCustomDescriptorPath.equals(myCustomDescriptorPath)) return false;
    if (!copy.myPackageFileName.equals(myPackageFileName)) return false;
    if (!FlexUtils.equalLists(copy.myFilesToPackage, myFilesToPackage)) return false;
    if (!copy.mySigningOptions.equals(mySigningOptions)) return false;

    return true;
  }
}
