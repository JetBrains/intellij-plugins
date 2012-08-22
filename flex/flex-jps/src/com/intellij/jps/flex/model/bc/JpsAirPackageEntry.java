package com.intellij.jps.flex.model.bc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;

public interface JpsAirPackageEntry extends JpsElement {

  @NotNull
  String getFilePath();

  @NotNull
  String getPathInPackage();
}
