package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.lang.javascript.flex.actions.AirSigningOptions;
import com.intellij.lang.javascript.flex.actions.airinstaller.AirInstallerParametersBase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AirPackagingOptions {

  boolean isUseGeneratedDescriptor();

  @NotNull
  String getCustomDescriptorPath();

  @NotNull
  String getPackageFileName();

  @NotNull
  List<AirInstallerParametersBase.FilePathAndPathInPackage> getFilesToPackage();

  @NotNull
  AirSigningOptions getSigningOptions();
}
