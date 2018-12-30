package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
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
  private final NodePackage myNodePackage;

  @Nullable
  private final String myCustomConfigFilePath;

  private final boolean myCustomConfigFileUsed;
  @Nullable
  private final String myRulesDirectory;
  private final boolean myAllowJs;
  private final NodePackageRef myNodePackageRef;

  private TsLintState(@NotNull NodeJsInterpreterRef nodePath,
                      @NotNull NodePackage nodePackage,
                      boolean customConfigFileUsed,
                      @Nullable String customConfigFilePath,
                      @Nullable String rulesDirectory, boolean allowJs) {
    myCustomConfigFileUsed = customConfigFileUsed;
    myCustomConfigFilePath = customConfigFilePath;
    myInterpreterRef = nodePath;
    myNodePackage = nodePackage;
    myRulesDirectory = rulesDirectory;
    myAllowJs = allowJs;
    myNodePackageRef = NodePackageRef.create(nodePackage);
  }

  public boolean isCustomConfigFileUsed() {
    return myCustomConfigFileUsed;
  }

  @Nullable
  public String getCustomConfigFilePath() {
    return myCustomConfigFilePath;
  }

  @Override
  @NotNull
  public NodeJsInterpreterRef getInterpreterRef() {
    return myInterpreterRef;
  }

  @Nullable
  public String getRulesDirectory() {
    return myRulesDirectory;
  }

  public boolean isAllowJs() {
    return myAllowJs;
  }

  @NotNull
  public NodePackage getNodePackage() {
    return myNodePackage;
  }

  @NotNull
  @Override
  public NodePackageRef getNodePackageRef() {
    return myNodePackageRef;
  }

  @Override
  public TsLintState withLinterPackage(@NotNull NodePackageRef nodePackage) {
    NodePackage constantPackage = nodePackage.getConstantPackage();
    assert constantPackage != null : this.getClass().getSimpleName() + " does not support non-constant package refs";
    return new Builder(this).setNodePackage(constantPackage).build();
  }

  @Override
  public TsLintState withInterpreterRef(NodeJsInterpreterRef ref) {
    return new TsLintState(ref, myNodePackage, myCustomConfigFileUsed, myCustomConfigFilePath, myRulesDirectory, myAllowJs);
  }

  public static class Builder {
    private boolean myCustomConfigFileUsed = false;
    private String myCustomConfigFilePath = "";
    private NodeJsInterpreterRef myInterpreterRef = NodeJsInterpreterRef.createProjectRef();
    private NodePackage myNodePackage = new NodePackage("");
    private boolean myAllowJs;

    @Nullable
    private String myRulesDirectory;

    public Builder() {
    }

    public Builder(@NotNull final TsLintState state) {
      myCustomConfigFileUsed = state.isCustomConfigFileUsed();
      myCustomConfigFilePath = state.getCustomConfigFilePath();
      myInterpreterRef = state.getInterpreterRef();
      myNodePackage = state.getNodePackage();
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

    public Builder setNodePackage(NodePackage nodePackage) {
      myNodePackage = nodePackage;
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
      return new TsLintState(myInterpreterRef, myNodePackage, myCustomConfigFileUsed, myCustomConfigFilePath, myRulesDirectory, myAllowJs);
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
    if (!Objects.equals(myNodePackage, state.myNodePackage)) return false;
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
    result = 31 * result + myNodePackage.hashCode();
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
           ", myPackagePath='" + myNodePackage + '\'' +
           ", myCustomConfigFilePath='" + myCustomConfigFilePath + '\'' +
           ", myCustomConfigFileUsed=" + myCustomConfigFileUsed +
           ", myRulesDirectory='" + myRulesDirectory + '\'' +
           ", myAllowJs=" + myAllowJs +
           '}';
  }
}
