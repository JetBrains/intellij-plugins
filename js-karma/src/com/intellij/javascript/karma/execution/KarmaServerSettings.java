package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class KarmaServerSettings {

  private final boolean myWithCoverage;
  private final boolean myDebug;
  private final NodeJsLocalInterpreter myNodeInterpreter;
  private final String myNodeOptions;
  private final NodePackage myKarmaPackage;
  private final String myConfigFilePath;
  private final String myBrowsers;
  private final String myWorkingDirectory;
  private final EnvironmentVariablesData myEnvData;

  private KarmaServerSettings(@NotNull Builder builder) {
    myWithCoverage = builder.myWithCoverage;
    myDebug = builder.myDebug;
    myNodeInterpreter = builder.myNodeInterpreter;
    myNodeOptions = builder.myRunSettings.getNodeOptions();
    myKarmaPackage = builder.myKarmaPackage;
    myConfigFilePath = FileUtil.toSystemDependentName(builder.myRunSettings.getConfigPath());
    myBrowsers = builder.myRunSettings.getBrowsers();
    myWorkingDirectory = builder.myRunSettings.getWorkingDirectorySystemDependent();
    myEnvData = builder.myRunSettings.getEnvData();
  }

  @NotNull
  public NodeJsLocalInterpreter getNodeInterpreter() {
    return myNodeInterpreter;
  }

  @NotNull
  public String getNodeOptions() {
    return myNodeOptions;
  }

  @NotNull
  public NodePackage getKarmaPackage() {
    return myKarmaPackage;
  }

  public boolean isWithCoverage() {
    return myWithCoverage;
  }

  public boolean isDebug() {
    return myDebug;
  }

  @NotNull
  public File getConfigurationFile() {
    return new File(myConfigFilePath);
  }

  @NotNull
  public String getConfigurationFilePath() {
    return myConfigFilePath;
  }

  @NotNull
  public String getBrowsers() {
    return myBrowsers;
  }

  @NotNull
  public String getWorkingDirectorySystemDependent() {
    return myWorkingDirectory;
  }

  @NotNull
  public EnvironmentVariablesData getEnvData() {
    return myEnvData;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    KarmaServerSettings that = (KarmaServerSettings)o;

    return myWithCoverage == that.myWithCoverage &&
           myDebug == that.myDebug &&
           myNodeInterpreter.getInterpreterSystemIndependentPath().equals(that.myNodeInterpreter.getInterpreterSystemIndependentPath()) &&
           myNodeOptions.equals(that.myNodeOptions) &&
           myKarmaPackage.equals(that.myKarmaPackage) &&
           myConfigFilePath.equals(that.myConfigFilePath) &&
           myBrowsers.equals(that.myBrowsers) &&
           myWorkingDirectory.equals(that.myWorkingDirectory) &&
           myEnvData.equals(that.myEnvData);
  }

  @Override
  public int hashCode() {
    int result = myWithCoverage ? 1 : 0;
    result = 31 * result + (myDebug ? 1 : 0);
    result = 31 * result + myNodeInterpreter.getInterpreterSystemIndependentPath().hashCode();
    result = 31 * result + myNodeOptions.hashCode();
    result = 31 * result + myKarmaPackage.hashCode();
    result = 31 * result + myConfigFilePath.hashCode();
    result = 31 * result + myBrowsers.hashCode();
    result = 31 * result + myWorkingDirectory.hashCode();
    result = 31 * result + myEnvData.hashCode();
    return result;
  }

  public static class Builder {
    private NodeJsLocalInterpreter myNodeInterpreter;
    private NodePackage myKarmaPackage;
    private KarmaRunSettings myRunSettings;
    private boolean myWithCoverage;
    private boolean myDebug;

    @NotNull
    public Builder setNodeInterpreter(@NotNull NodeJsLocalInterpreter interpreter) {
      myNodeInterpreter = interpreter;
      return this;
    }

    @NotNull
    public Builder setKarmaPackage(@NotNull NodePackage karmaPackage) {
      myKarmaPackage = karmaPackage;
      return this;
    }

    @NotNull
    public Builder setRunSettings(@NotNull KarmaRunSettings runSettings) {
      myRunSettings = runSettings;
      return this;
    }

    @NotNull
    public Builder setWithCoverage(boolean withCoverage) {
      myWithCoverage = withCoverage;
      return this;
    }

    @NotNull
    public Builder setDebug(boolean debug) {
      myDebug = debug;
      return this;
    }

    @NotNull
    public KarmaServerSettings build() {
      if (myNodeInterpreter == null) {
        throw new RuntimeException("Unspecified Node.js interpreter");
      }
      if (myKarmaPackage == null) {
        throw new RuntimeException("Unspecified karma package");
      }
      if (myRunSettings == null) {
        throw new RuntimeException("Unspecified run settings");
      }
      return new KarmaServerSettings(this);
    }
  }
}
