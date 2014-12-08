package com.jetbrains.lang.dart.projectWizard;

import com.intellij.ide.browsers.chrome.ChromeSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartProjectWizardData {
  @NotNull public final String dartSdkPath;
  @NotNull public final String dartiumPath;
  @NotNull public final ChromeSettings dartiumSettings;
  @Nullable public final DartProjectTemplate myTemplate;

  public DartProjectWizardData(@NotNull final String dartSdkPath,
                               @NotNull final String dartiumPath,
                               @NotNull final ChromeSettings dartiumSettings,
                               @Nullable final DartProjectTemplate template) {
    this.dartSdkPath = dartSdkPath;
    this.dartiumPath = dartiumPath;
    this.dartiumSettings = dartiumSettings;
    myTemplate = template;
  }
}
