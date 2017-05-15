package com.intellij.javascript.karma.execution;

import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class KarmaServerSettings {

  private final NodeJsLocalInterpreter myNodeInterpreter;
  private final NodePackage myKarmaPackage;
  private final KarmaRunSettings myRunSettings;
  private final boolean myWithCoverage;
  private final boolean myDebug;

  private KarmaServerSettings(@NotNull Builder builder) {
    myNodeInterpreter = builder.myNodeInterpreter;
    myKarmaPackage = builder.myKarmaPackage;
    myRunSettings = builder.myRunSettings;
    myWithCoverage = builder.myWithCoverage;
    myDebug = builder.myDebug;
  }

  @NotNull
  public NodeJsLocalInterpreter getNodeInterpreter() {
    return myNodeInterpreter;
  }

  @NotNull
  public NodePackage getKarmaPackage() {
    return myKarmaPackage;
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

    return myKarmaPackage.equals(that.myKarmaPackage) &&
           myNodeInterpreter.getInterpreterSystemIndependentPath().equals(that.myNodeInterpreter.getInterpreterSystemIndependentPath()) &&
           myRunSettings.equals(that.myRunSettings) &&
           myWithCoverage == that.myWithCoverage &&
           myDebug == that.myDebug;
  }

  @Override
  public int hashCode() {
    int result = myNodeInterpreter.getInterpreterSystemIndependentPath().hashCode();
    result = 31 * result + myKarmaPackage.hashCode();
    result = 31 * result + myRunSettings.hashCode();
    result = 31 * result + (myWithCoverage ? 1 : 0);
    result = 31 * result + (myDebug ? 1 : 0);
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
