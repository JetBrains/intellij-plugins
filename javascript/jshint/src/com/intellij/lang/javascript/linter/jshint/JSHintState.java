package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.linter.JSLinterState;
import com.intellij.lang.javascript.linter.jshint.version.JSHintVersionUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable state of JSHint:
 * <ul>
 *   <li>enable/disable</li>
 *   <li>version</li>
 *   <li>option's value list</li>
 * </ul>.
 *
 * @author Sergey Simonchik
 */
public final class JSHintState implements JSLinterState {

  private final JSHintOptionsState myOptionsState;
  private final String myVersion;
  private final boolean myConfigFileUsed;
  private final boolean myCustomConfigFileUsed;
  private final String myCustomConfigFilePath;

  private JSHintState(@NotNull JSHintOptionsState optionsState,
                      @NotNull String version,
                      boolean configFileUsed,
                      boolean customConfigFileUsed,
                      @NotNull String customConfigFilePath) {
    myOptionsState = optionsState;
    myVersion = version;
    myConfigFileUsed = configFileUsed;
    myCustomConfigFileUsed = customConfigFileUsed;
    myCustomConfigFilePath = customConfigFilePath;
  }

  public @NotNull JSHintOptionsState getOptionsState() {
    return myOptionsState;
  }

  public boolean isCustomConfigFileUsed() {
    return myCustomConfigFileUsed;
  }

  public @NotNull String getCustomConfigFilePath() {
    return myCustomConfigFilePath;
  }

  public @NotNull String getVersion() {
    return myVersion;
  }

  public boolean isConfigFileUsed() {
    return myConfigFileUsed;
  }

  public static class Builder {
    private JSHintOptionsState myOptionsState;
    private String myVersion = JSHintVersionUtil.BUNDLED_VERSION;
    private boolean myConfigFileUsed = false;
    private boolean myCustomConfigFileUsed = false;
    private String myCustomConfigFilePath = "";

    public Builder() {}

    public Builder(@NotNull JSHintState state) {
      setOptionsState(state.getOptionsState());
      setVersion(state.getVersion());
      setConfigFileUsed(state.isConfigFileUsed());
      setCustomConfigFileUsed(state.isCustomConfigFileUsed());
      setCustomConfigFilePath(state.getCustomConfigFilePath());
    }

    public Builder setOptionsState(@NotNull JSHintOptionsState optionsState) {
      myOptionsState = optionsState;
      return this;
    }

    public @NotNull Builder setVersion(@NotNull String version) {
      myVersion = version;
      return this;
    }

    public @NotNull Builder setConfigFileUsed(boolean configFileUsed) {
      myConfigFileUsed = configFileUsed;
      return this;
    }

    public @NotNull Builder setCustomConfigFileUsed(boolean customConfigFileUsed) {
      myCustomConfigFileUsed = customConfigFileUsed;
      return this;
    }

    public @NotNull Builder setCustomConfigFilePath(@NotNull String customConfigFilePath) {
      myCustomConfigFilePath = customConfigFilePath;
      return this;
    }

    public JSHintState build() {
      return new JSHintState(myOptionsState, myVersion, myConfigFileUsed, myCustomConfigFileUsed, myCustomConfigFilePath);
    }
  }

  @Override
  public String toString() {
    return "JSHintState{version=" + myVersion +
           ", options=" + myOptionsState +
           ", configFileUsed=" + myConfigFileUsed +
           ", customConfigFileUsed=" + myCustomConfigFileUsed +
           ", customConfigFilePath=" + myCustomConfigFilePath +
           '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    JSHintState state = (JSHintState)o;

    if (!myVersion.equals(state.getVersion())) return false;
    if (myConfigFileUsed != state.isConfigFileUsed()) return false;
    if (myCustomConfigFileUsed != state.isCustomConfigFileUsed()) return false;
    if (!myCustomConfigFilePath.equals(state.getCustomConfigFilePath())) return false;
    return myOptionsState.equals(state.myOptionsState);
  }

  @Override
  public int hashCode() {
    int result = myOptionsState.hashCode();
    result = 31 * result + myVersion.hashCode();
    result = 31 * result + (myConfigFileUsed ? 1 : 0);
    result = 31 * result + (myCustomConfigFileUsed ? 1 : 0);
    result = 31 * result + myCustomConfigFilePath.hashCode();
    return result;
  }
}
