package com.intellij.coldFusion.projectWizard;

/**
 * Created by jetbrains on 11/02/16.
 */

import com.intellij.coldFusion.model.CfmlLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CfmlProjectWizardData {
  @Nullable public final String serverPath;
  @Nullable public final String serverPort;
  @NotNull public final String myCfmlLanguage;

  public CfmlProjectWizardData(@Nullable final String serverPath,
                               String serverPort,  @NotNull String language) {
    this.serverPath = serverPath;
    this.serverPort = serverPort;
    myCfmlLanguage = language;
  }
}
