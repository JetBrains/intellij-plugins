// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class KarmaServerSettings {

  private final boolean myWithCoverage;
  private final boolean myDebug;
  private final NodeJsInterpreter myNodeInterpreter;
  private final String myNodeOptions;
  private final NodePackage myKarmaPackage;
  private final String myConfigFilePath;
  private final String myKarmaOptions;
  private final String myWorkingDirectory;
  private final EnvironmentVariablesData myEnvData;

  private KarmaServerSettings(@NotNull Builder builder) {
    myWithCoverage = builder.myWithCoverage;
    myDebug = builder.myDebug;
    myNodeInterpreter = builder.myNodeInterpreter;
    myNodeOptions = builder.myRunSettings.getNodeOptions();
    myKarmaPackage = builder.myKarmaPackage;
    myConfigFilePath = builder.myRunSettings.getConfigPathSystemDependent();
    myKarmaOptions = builder.myRunSettings.getKarmaOptions();
    myWorkingDirectory = builder.myRunSettings.getWorkingDirectorySystemDependent();
    myEnvData = builder.myRunSettings.getEnvData();
  }

  @NotNull
  public NodeJsInterpreter getNodeInterpreter() {
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
  public String getKarmaOptions() {
    return myKarmaOptions;
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
           myNodeInterpreter.equals(that.myNodeInterpreter) &&
           myNodeOptions.equals(that.myNodeOptions) &&
           myKarmaPackage.equals(that.myKarmaPackage) &&
           myConfigFilePath.equals(that.myConfigFilePath) &&
           myKarmaOptions.equals(that.myKarmaOptions) &&
           myWorkingDirectory.equals(that.myWorkingDirectory) &&
           myEnvData.equals(that.myEnvData);
  }

  @Override
  public int hashCode() {
    int result = myWithCoverage ? 1 : 0;
    result = 31 * result + (myDebug ? 1 : 0);
    result = 31 * result + myNodeInterpreter.hashCode();
    result = 31 * result + myNodeOptions.hashCode();
    result = 31 * result + myKarmaPackage.hashCode();
    result = 31 * result + myConfigFilePath.hashCode();
    result = 31 * result + myKarmaOptions.hashCode();
    result = 31 * result + myWorkingDirectory.hashCode();
    result = 31 * result + myEnvData.hashCode();
    return result;
  }

  public static class Builder {
    private NodeJsInterpreter myNodeInterpreter;
    private NodePackage myKarmaPackage;
    private KarmaRunSettings myRunSettings;
    private boolean myWithCoverage;
    private boolean myDebug;

    @NotNull
    public Builder setNodeInterpreter(@NotNull NodeJsInterpreter interpreter) {
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
