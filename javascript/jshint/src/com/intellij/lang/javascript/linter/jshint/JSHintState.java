package com.intellij.lang.javascript.linter.jshint;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable state of JSHint:
 * <ul>
 *   <li>enable/disable</li>
 *   <li>npm package</li>
 *   <li>option's value list</li>
 * </ul>.
 *
 * @author Sergey Simonchik
 */
public final class JSHintState implements JSNpmLinterState<JSHintState> {

  private final JSHintOptionsState myOptionsState;
  private final boolean myConfigFileUsed;
  private final boolean myCustomConfigFileUsed;
  private final String myCustomConfigFilePath;
  private final @NotNull NodePackageRef myPackageRef;

  private JSHintState(@NotNull JSHintOptionsState optionsState,
                      boolean configFileUsed,
                      boolean customConfigFileUsed,
                      @NotNull String customConfigFilePath,
                      @NotNull NodePackageRef packageRef) {
    myOptionsState = optionsState;
    myConfigFileUsed = configFileUsed;
    myCustomConfigFileUsed = customConfigFileUsed;
    myCustomConfigFilePath = customConfigFilePath;
    myPackageRef = packageRef;
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

  public boolean isConfigFileUsed() {
    return myConfigFileUsed;
  }

  @Override
  public @NotNull NodePackageRef getNodePackageRef() {
    return myPackageRef;
  }

  @Override
  public JSHintState withLinterPackage(@NotNull NodePackageRef nodePackage) {
    return new Builder(this)
      .setPackageRef(nodePackage)
      .build();
  }

  public static class Builder {
    private JSHintOptionsState myOptionsState;
    private boolean myConfigFileUsed = false;
    private boolean myCustomConfigFileUsed = false;
    private String myCustomConfigFilePath = "";
    private NodePackageRef myPackageRef = NodePackageRef.create(new NodePackage(""));

    public Builder() {}

    public Builder(@NotNull JSHintState state) {
      setOptionsState(state.getOptionsState());
      setConfigFileUsed(state.isConfigFileUsed());
      setCustomConfigFileUsed(state.isCustomConfigFileUsed());
      setCustomConfigFilePath(state.getCustomConfigFilePath());
      setPackageRef(state.getNodePackageRef());
    }

    public Builder setOptionsState(@NotNull JSHintOptionsState optionsState) {
      myOptionsState = optionsState;
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

    public @NotNull Builder setPackageRef(@NotNull NodePackageRef packageRef) {
      myPackageRef = packageRef;
      return this;
    }

    public JSHintState build() {
      return new JSHintState(myOptionsState, myConfigFileUsed, myCustomConfigFileUsed, myCustomConfigFilePath, myPackageRef);
    }
  }

  @Override
  public String toString() {
    return "JSHintState{packageRef=" + myPackageRef +
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

    if (myConfigFileUsed != state.isConfigFileUsed()) return false;
    if (myCustomConfigFileUsed != state.isCustomConfigFileUsed()) return false;
    if (!myCustomConfigFilePath.equals(state.getCustomConfigFilePath())) return false;
    if (!myPackageRef.equals(state.getNodePackageRef())) return false;
    return myOptionsState.equals(state.myOptionsState);
  }

  @Override
  public int hashCode() {
    int result = myOptionsState.hashCode();
    result = 31 * result + (myConfigFileUsed ? 1 : 0);
    result = 31 * result + (myCustomConfigFileUsed ? 1 : 0);
    result = 31 * result + myCustomConfigFilePath.hashCode();
    result = 31 * result + myPackageRef.hashCode();
    return result;
  }
}
