package com.intellij.flex.model.bc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;

import java.util.List;


public interface JpsAirPackagingOptions extends JpsElement {

  boolean isUseGeneratedDescriptor();

  void setUseGeneratedDescriptor(boolean useGeneratedDescriptor);

  @NotNull
  String getCustomDescriptorPath();

  void setCustomDescriptorPath(@NotNull String customDescriptorPath);

  @NotNull
  String getPackageFileName();

  void setPackageFileName(@NotNull String packageFileName);

  @NotNull
  List<JpsAirPackageEntry> getFilesToPackage();

  @NotNull
  JpsAirSigningOptions getSigningOptions();
}
