package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Irina.Chernushina on 6/3/2015.
 */
public class TsLintState implements JSNpmLinterState<TsLintState> {

  @NotNull
  private final NodeJsInterpreterRef myInterpreterRef;

  @NotNull
  private final String myPackagePath;

  @Nullable
  private String myCustomConfigFilePath;

  private final boolean myCustomConfigFileUsed;
  @Nullable
  private final String myRulesDirectory;
  private final boolean myAllowJs;

  private TsLintState(@NotNull NodeJsInterpreterRef nodePath,
                      @NotNull String packagePath,
                      boolean customConfigFileUsed,
                      @Nullable String customConfigFilePath,
                      @Nullable String rulesDirectory, boolean allowJs) {
    myCustomConfigFileUsed = customConfigFileUsed;
    myCustomConfigFilePath = customConfigFilePath;
    myInterpreterRef = nodePath;
    myPackagePath = packagePath;
    myRulesDirectory = rulesDirectory;
    myAllowJs = allowJs;
  }

  public boolean isCustomConfigFileUsed() {
    return myCustomConfigFileUsed;
  }

  @Nullable
  public String getCustomConfigFilePath() {
    return myCustomConfigFilePath;
  }

  public void setCustomConfigFilePath(@Nullable String customConfigFilePath) {
    myCustomConfigFilePath = customConfigFilePath;
  }

  @Override
  @NotNull
  public NodeJsInterpreterRef getInterpreterRef() {
    return myInterpreterRef;
  }

  @NotNull
  public String getPackagePath() {
    return myPackagePath;
  }

  @Nullable
  public String getRulesDirectory() {
    return myRulesDirectory;
  }

  public boolean isAllowJs() {
    return myAllowJs;
  }

  @NotNull
  @Override
  public NodePackage getNodePackage() {
    return new NodePackage(myPackagePath);
  }

  @Override
  public TsLintState withLinterPackagePath(@NotNull String path) {
    return new TsLintState(myInterpreterRef, path, myCustomConfigFileUsed, myCustomConfigFilePath, myRulesDirectory, myAllowJs);
  }

  @Override
  public TsLintState withInterpreterRef(NodeJsInterpreterRef ref) {
    return new TsLintState(ref, myPackagePath, myCustomConfigFileUsed, myCustomConfigFilePath, myRulesDirectory, myAllowJs);
  }

  public static class Builder {
    private boolean myCustomConfigFileUsed = false;
    private String myCustomConfigFilePath = "";
    private NodeJsInterpreterRef myInterpreterRef = NodeJsInterpreterRef.createProjectRef();
    private String myPackagePath = "";
    private boolean myAllowJs;

    @Nullable
    private String myRulesDirectory;

    public Builder() {
    }

    public Builder(@NotNull final TsLintState state) {
      myCustomConfigFileUsed = state.isCustomConfigFileUsed();
      myCustomConfigFilePath = state.getCustomConfigFilePath();
      myInterpreterRef = state.getInterpreterRef();
      myPackagePath = state.getPackagePath();
      myRulesDirectory = state.getRulesDirectory();
      myAllowJs = state.isAllowJs();
    }

    public Builder setCustomConfigFileUsed(boolean customConfigFileUsed) {
      myCustomConfigFileUsed = customConfigFileUsed;
      return this;
    }

    public Builder setCustomConfigFilePath(String customConfigFilePath) {
      myCustomConfigFilePath = customConfigFilePath;
      return this;
    }

    public Builder setNodePath(NodeJsInterpreterRef nodePath) {
      myInterpreterRef = nodePath;
      return this;
    }

    public Builder setPackagePath(String packagePath) {
      myPackagePath = packagePath;
      return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Builder setRulesDirectory(@Nullable String rulesDirectory) {
      myRulesDirectory = rulesDirectory;
      return this;
    }

    public Builder setAllowJs(boolean allowJs) {
      myAllowJs = allowJs;
      return this;
    }

    public TsLintState build() {
      return new TsLintState(myInterpreterRef, myPackagePath, myCustomConfigFileUsed, myCustomConfigFilePath, myRulesDirectory, myAllowJs);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TsLintState state = (TsLintState)o;

    if (myCustomConfigFileUsed != state.myCustomConfigFileUsed) return false;
    if (myAllowJs != state.myAllowJs) return false;
    if (!myInterpreterRef.equals(state.myInterpreterRef)) return false;
    if (!Objects.equals(myPackagePath, state.myPackagePath)) return false;
    if (myCustomConfigFilePath != null
        ? !myCustomConfigFilePath.equals(state.myCustomConfigFilePath)
        : state.myCustomConfigFilePath != null) {
      return false;
    }
    if (myRulesDirectory != null ? !myRulesDirectory.equals(state.myRulesDirectory) : state.myRulesDirectory != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myInterpreterRef.hashCode();
    result = 31 * result + myPackagePath.hashCode();
    result = 31 * result + (myCustomConfigFilePath != null ? myCustomConfigFilePath.hashCode() : 0);
    result = 31 * result + (myCustomConfigFileUsed ? 1 : 0);
    result = 31 * result + (myRulesDirectory != null ? myRulesDirectory.hashCode() : 0);
    result = 31 * result + (myAllowJs ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "TsLintState{" +
           "myInterpreterRef=" + myInterpreterRef +
           ", myPackagePath='" + myPackagePath + '\'' +
           ", myCustomConfigFilePath='" + myCustomConfigFilePath + '\'' +
           ", myCustomConfigFileUsed=" + myCustomConfigFileUsed +
           ", myRulesDirectory='" + myRulesDirectory + '\'' +
           ", myAllowJs=" + myAllowJs +
           '}';
  }
}
