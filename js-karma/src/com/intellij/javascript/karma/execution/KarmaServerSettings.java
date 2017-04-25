package com.intellij.javascript.karma.execution;

import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class KarmaServerSettings {

  private final NodeJsLocalInterpreter myNodeInterpreter;
  private final String myKarmaPackageDirPath;
  private final KarmaRunSettings myRunSettings;
  private final boolean myWithCoverage;
  private final boolean myDebug;

  private KarmaServerSettings(@NotNull Builder builder) {
    myNodeInterpreter = builder.myNodeInterpreter;
    myKarmaPackageDirPath = builder.myKarmaPackageDirPath;
    myRunSettings = builder.myRunSettings;
    myWithCoverage = builder.myWithCoverage;
    myDebug = builder.myDebug;
  }

  @NotNull
  public NodeJsLocalInterpreter getNodeInterpreter() {
    return myNodeInterpreter;
  }

  @NotNull
  public File getKarmaPackageDir() {
    return new File(myKarmaPackageDirPath);
  }

  @NotNull
  public KarmaRunSettings getRunSettings() {
    return myRunSettings;
  }

  public boolean isWithCoverage() {
    return myWithCoverage;
  }

  public boolean isDebug() {
    return myDebug;
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
           myNodeInterpreter.getInterpreterSystemIndependentPath().equals(that.myNodeInterpreter.getInterpreterSystemIndependentPath()) &&
           myRunSettings.equals(that.myRunSettings) &&
           myWithCoverage == that.myWithCoverage &&
           myDebug == that.myDebug;
  }

  @Override
  public int hashCode() {
    int result = myNodeInterpreter.getInterpreterSystemIndependentPath().hashCode();
    result = 31 * result + myKarmaPackageDirPath.hashCode();
    result = 31 * result + myRunSettings.hashCode();
    result = 31 * result + (myWithCoverage ? 1 : 0);
    result = 31 * result + (myDebug ? 1 : 0);
    return result;
  }

  public static class Builder {
    private NodeJsLocalInterpreter myNodeInterpreter;
    private String myKarmaPackageDirPath;
    private KarmaRunSettings myRunSettings;
    private boolean myWithCoverage;
    private boolean myDebug;

    @NotNull
    public Builder setNodeInterpreter(@NotNull NodeJsLocalInterpreter interpreter) {
      myNodeInterpreter = interpreter;
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
      if (myKarmaPackageDirPath == null) {
        throw new RuntimeException("Path to karma package isn't set!");
      }
      if (myRunSettings == null) {
        throw new RuntimeException("Run settings aren't set!");
      }
      return new KarmaServerSettings(this);
    }
  }
}
