package com.intellij.javascript.bower;

import com.intellij.webcore.packaging.InstalledPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BowerInstalledPackage extends InstalledPackage {

  private final String myLatestVersion;

  public BowerInstalledPackage(@NotNull String name, @Nullable String version, @Nullable String latestVersion) {
    super(name, version);
    myLatestVersion = latestVersion;
  }

  public @Nullable String getLatestVersion() {
    return myLatestVersion;
  }

}
