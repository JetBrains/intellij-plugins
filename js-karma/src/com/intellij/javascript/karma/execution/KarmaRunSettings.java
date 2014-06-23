package com.intellij.javascript.karma.execution;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class KarmaRunSettings {

  private final String myConfigPath;
  private final ImmutableMap<String, String> myEnvVars;
  private final boolean myPassParentEnvVars;
  private final String myBrowsers;

  public KarmaRunSettings(@NotNull String configPath,
                          @NotNull Map<String, String> envVars,
                          boolean passParentEnvVars,
                          @NotNull String browsers) {
    myConfigPath = configPath;
    myEnvVars = ImmutableMap.copyOf(envVars);
    myPassParentEnvVars = passParentEnvVars;
    myBrowsers = browsers;
  }

  @NotNull
  public String getConfigPath() {
    return myConfigPath;
  }

  @NotNull
  public ImmutableMap<String, String> getEnvVars() {
    return myEnvVars;
  }

  public boolean isPassParentEnvVars() {
    return myPassParentEnvVars;
  }

  @NotNull
  public String getBrowsers() {
    return myBrowsers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    KarmaRunSettings that = (KarmaRunSettings)o;

    return  myPassParentEnvVars == that.myPassParentEnvVars &&
            myConfigPath.equals(that.myConfigPath) &&
            myEnvVars.equals(that.myEnvVars) &&
            myBrowsers.equals(that.myBrowsers);
  }

  @Override
  public int hashCode() {
    int result = myConfigPath.hashCode();
    result = 31 * result + myEnvVars.hashCode();
    result = 31 * result + (myPassParentEnvVars ? 1 : 0);
    result = 31 * result + myBrowsers.hashCode();
    return result;
  }

  public static class Builder {

    public static final boolean DEFAULT_PASS_PARENT_ENV_VARS = true;

    private String myConfigPath = "";
    private Map<String, String> myEnvVars = Collections.emptyMap();
    private boolean myPassParentEnvVars = DEFAULT_PASS_PARENT_ENV_VARS;
    private String myBrowsers = "";

    public Builder() {}

    @NotNull
    public Builder setConfigPath(@NotNull String configPath) {
      myConfigPath = configPath;
      return this;
    }

    @NotNull
    public Builder setEnvVars(@NotNull Map<String, String> envVars) {
      myEnvVars = ImmutableMap.copyOf(envVars);
      return this;
    }

    @NotNull
    public Builder setPassParentEnvVars(boolean passParentEnvVars) {
      myPassParentEnvVars = passParentEnvVars;
      return this;
    }

    @NotNull
    public Builder setBrowsers(@NotNull String browsers) {
      myBrowsers = browsers;
      return this;
    }

    @NotNull
    public KarmaRunSettings build() {
      return new KarmaRunSettings(myConfigPath, myEnvVars, myPassParentEnvVars, myBrowsers);
    }
  }
}
