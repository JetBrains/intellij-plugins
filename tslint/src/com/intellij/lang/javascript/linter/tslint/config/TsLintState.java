package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.lang.javascript.linter.JSLinterState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Irina.Chernushina on 6/3/2015.
 */
public class TsLintState implements JSLinterState {

  @NotNull
  private final NodeJsInterpreterRef myInterpreterRef;

  private final String myPackagePath;

  @Nullable
  private String myCustomConfigFilePath;

  private final boolean myCustomConfigFileUsed;
  @Nullable
  private final String myRulesDirectory;

  public TsLintState(@NotNull NodeJsInterpreterRef nodePath,
                     @Nullable String packagePath,
                     boolean customConfigFileUsed,
                     @Nullable String customConfigFilePath,
                     @Nullable String rulesDirectory) {
    myCustomConfigFileUsed = customConfigFileUsed;
    myCustomConfigFilePath = customConfigFilePath;
    myInterpreterRef = nodePath;
    myPackagePath = packagePath;
    myRulesDirectory = rulesDirectory;
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

  @NotNull
  public NodeJsInterpreterRef getInterpreterRef() {
    return myInterpreterRef;
  }

  public String getPackagePath() {
    return myPackagePath;
  }

  @Nullable
  public String getRulesDirectory() {
    return myRulesDirectory;
  }

  public static class Builder {
    private boolean myCustomConfigFileUsed = false;
    private String myCustomConfigFilePath = "";
    private NodeJsInterpreterRef myInterpreterRef = NodeJsInterpreterRef.createProjectRef();
    private String myPackagePath = "";

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

    public Builder setRulesDirectory(@Nullable String rulesDirectory) {
      myRulesDirectory = rulesDirectory;
      return this;
    }

    public TsLintState build() {
      return new TsLintState(myInterpreterRef, myPackagePath, myCustomConfigFileUsed, myCustomConfigFilePath, myRulesDirectory);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TsLintState state = (TsLintState)o;

    if (myCustomConfigFileUsed != state.myCustomConfigFileUsed) return false;
    if (myCustomConfigFilePath != null
        ? !myCustomConfigFilePath.equals(state.myCustomConfigFilePath)
        : state.myCustomConfigFilePath != null) {
      return false;
    }
    if (!myInterpreterRef.equals(state.myInterpreterRef)) return false;
    if (myPackagePath != null ? !myPackagePath.equals(state.myPackagePath) : state.myPackagePath != null) return false;
    if (myRulesDirectory != null ? !myRulesDirectory.equals(state.myRulesDirectory) : state.myRulesDirectory != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = (myCustomConfigFileUsed ? 1 : 0);
    result = 31 * result + (myCustomConfigFilePath != null ? myCustomConfigFilePath.hashCode() : 0);
    result = 31 * result + myInterpreterRef.hashCode();
    result = 31 * result + (myPackagePath != null ? myPackagePath.hashCode() : 0);
    result = 31 * result + (myRulesDirectory != null ? myRulesDirectory.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "TsLintState{" +
           "myCustomConfigFileUsed=" + myCustomConfigFileUsed +
           ", myCustomConfigFilePath='" + myCustomConfigFilePath + '\'' +
           ", myNodePath='" + myInterpreterRef.getReferenceName() + '\'' +
           ", myPackagePath='" + myPackagePath + '\'' +
           ", myRulesDirectory='" + myRulesDirectory + '\'' +
           '}';
  }
}
