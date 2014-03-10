package com.intellij.javascript.karma.execution;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * @author Sergey Simonchik
 */
public class KarmaRunSettings {

  private final String myConfigPath;
  private final ImmutableMap<String, String> myEnvVars;
  private final boolean myPassParentEnvVars;

  public KarmaRunSettings(@NotNull String configPath,
                          @NotNull Map<String, String> envVars,
                          boolean passParentEnvVars) {
    myConfigPath = configPath;
    myEnvVars = ImmutableMap.copyOf(envVars);
    myPassParentEnvVars = passParentEnvVars;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    KarmaRunSettings settings = (KarmaRunSettings)o;

    return  myPassParentEnvVars == settings.myPassParentEnvVars &&
            myConfigPath.equals(settings.myConfigPath) &&
            myEnvVars.equals(settings.myEnvVars);
  }

  @Override
  public int hashCode() {
    int result = myConfigPath.hashCode();
    result = 31 * result + myEnvVars.hashCode();
    result = 31 * result + (myPassParentEnvVars ? 1 : 0);
    return result;
  }

  public static class Builder {

    public static final boolean DEFAULT_PASS_PARENT_ENV_VARS = true;

    private String myConfigPath = "";
    private Map<String, String> myEnvVars = Collections.emptyMap();
    private boolean myPassParentEnvVars = DEFAULT_PASS_PARENT_ENV_VARS;

    public Builder() {}

    @NotNull
    public Builder setConfigPath(@NotNull String configPath) {
      myConfigPath = configPath;
      return this;
    }

    public void setEnvVars(@NotNull Map<String, String> envVars) {
      myEnvVars = envVars;
    }

    public void setPassParentEnvVars(boolean passParentEnvVars) {
      myPassParentEnvVars = passParentEnvVars;
    }

    @NotNull
    public KarmaRunSettings build() {
      return new KarmaRunSettings(myConfigPath, myEnvVars, myPassParentEnvVars);
    }
  }

}
