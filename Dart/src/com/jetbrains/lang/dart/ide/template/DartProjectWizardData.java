package com.jetbrains.lang.dart.ide.template;

import com.intellij.ide.browsers.chrome.ChromeSettings;
import org.jetbrains.annotations.NotNull;

public class DartProjectWizardData {
  public final @NotNull String dartSdkPath;
  public final @NotNull String dartiumPath;
  public final @NotNull ChromeSettings dartiumSettings;

  public DartProjectWizardData(@NotNull final String dartSdkPath,
                               @NotNull final String dartiumPath,
                               @NotNull final ChromeSettings dartiumSettings) {
    this.dartSdkPath = dartSdkPath;
    this.dartiumPath = dartiumPath;
    this.dartiumSettings = dartiumSettings;
  }
}
