package com.intellij.javascript.karma.execution;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class KarmaRunSettings {

  private final String myConfigPath;

  public KarmaRunSettings(@NotNull String configPath) {
    myConfigPath = configPath;
  }

  @NotNull
  public String getConfigPath() {
    return myConfigPath;
  }

  public static class Builder {
    private String myConfigPath = "";

    public Builder() {}

    @NotNull
    public Builder setConfigPath(@NotNull String configPath) {
      myConfigPath = configPath;
      return this;
    }

    @NotNull
    public KarmaRunSettings build() {
      return new KarmaRunSettings(myConfigPath);
    }
  }

}
