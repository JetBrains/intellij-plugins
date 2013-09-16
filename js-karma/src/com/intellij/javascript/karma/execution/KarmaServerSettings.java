package com.intellij.javascript.karma.execution;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class KarmaServerSettings {

  private final String myNodeInterpreterPath;
  private final String myKarmaPackageDirPath;
  private final KarmaRunSettings myRunSettings;

  public KarmaServerSettings(@NotNull String nodeInterpreterPath,
                             @NotNull String karmaPackageDirPath,
                             @NotNull KarmaRunSettings runSettings) {
    myNodeInterpreterPath = nodeInterpreterPath;
    myKarmaPackageDirPath = karmaPackageDirPath;
    myRunSettings = runSettings;
  }

  @NotNull
  public String getNodeInterpreterPath() {
    return myNodeInterpreterPath;
  }

  @NotNull
  public File getKarmaPackageDir() {
    return new File(myKarmaPackageDirPath);
  }

  @NotNull
  public KarmaRunSettings getRunSettings() {
    return myRunSettings;
  }

  @NotNull
  public File getConfigurationFile() {
    return new File(myRunSettings.getConfigPath());
  }

  @NotNull
  public String getConfigurationFilePath() {
    return myRunSettings.getConfigPath();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    KarmaServerSettings that = (KarmaServerSettings)o;

    return myKarmaPackageDirPath.equals(that.myKarmaPackageDirPath) &&
           myNodeInterpreterPath.equals(that.myNodeInterpreterPath) &&
           myRunSettings.equals(that.myRunSettings);
  }

  @Override
  public int hashCode() {
    int result = myNodeInterpreterPath.hashCode();
    result = 31 * result + myKarmaPackageDirPath.hashCode();
    result = 31 * result + myRunSettings.hashCode();
    return result;
  }

  public static class Builder {
    private String myNodeInterpreterPath;
    private String myKarmaPackageDirPath;
    private KarmaRunSettings myRunSettings;

    @NotNull
    public Builder setNodeInterpreterPath(@NotNull String nodeInterpreterPath) {
      myNodeInterpreterPath = nodeInterpreterPath;
      return this;
    }

    @NotNull
    public Builder setKarmaPackageDirPath(@NotNull String karmaPackageDirPath) {
      myKarmaPackageDirPath = karmaPackageDirPath;
      return this;
    }

    @NotNull
    public Builder setRunSettings(@NotNull KarmaRunSettings runSettings) {
      myRunSettings = runSettings;
      return this;
    }

    @NotNull
    public KarmaServerSettings build() {
      if (myNodeInterpreterPath == null) {
        throw new RuntimeException("Path to node interpreter isn't set!");
      }
      if (myKarmaPackageDirPath == null) {
        throw new RuntimeException("Path to karma package isn't set!");
      }
      if (myRunSettings == null) {
        throw new RuntimeException("Run settings aren't set!");
      }
      return new KarmaServerSettings(myNodeInterpreterPath, myKarmaPackageDirPath, myRunSettings);
    }
  }

}
