package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.lang.javascript.flex.actions.AirSigningOptions;
import com.intellij.lang.javascript.flex.actions.airinstaller.AirInstallerParametersBase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ModifiableAirPackagingOptions extends AirPackagingOptions {

  void setUseGeneratedDescriptor(boolean useGeneratedDescriptor);

  void setCustomDescriptorPath(@NotNull String customDescriptorPath);

  void setPackageFileName(@NotNull String installerFileName);

  void setFilesToPackage(@NotNull List<AirInstallerParametersBase.FilePathAndPathInPackage> filesToPackage);

  void setSigningOptions(@NotNull AirSigningOptions signingOptions);
}
