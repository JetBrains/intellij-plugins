// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
public final class TsLintState implements JSNpmLinterState<TsLintState> {

  public static final TsLintState DEFAULT = new Builder().build();
  @NotNull
  private final NodeJsInterpreterRef myInterpreterRef;
  @NotNull
  private final NodePackageRef myNodePackageRef;
  @Nullable
  private final String myCustomConfigFilePath;

  private final boolean myCustomConfigFileUsed;
  @Nullable
  private final String myRulesDirectory;
  private final boolean myAllowJs;

  private TsLintState(@NotNull NodeJsInterpreterRef nodePath,
                      @NotNull NodePackageRef nodePackageRef,
                      boolean customConfigFileUsed,
                      @Nullable String customConfigFilePath,
                      @Nullable String rulesDirectory, boolean allowJs) {
    myCustomConfigFileUsed = customConfigFileUsed;
    myCustomConfigFilePath = customConfigFilePath;
    myInterpreterRef = nodePath;
    myRulesDirectory = rulesDirectory;
    myAllowJs = allowJs;
    myNodePackageRef = nodePackageRef;
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
  @Override
  public NodePackageRef getNodePackageRef() {
    return myNodePackageRef;
  }

  @Override
  public TsLintState withLinterPackage(@NotNull NodePackageRef nodePackageRef) {
    return new Builder(this).setNodePackageRef(nodePackageRef).build();
  }

  @Override
  public TsLintState withInterpreterRef(@NotNull NodeJsInterpreterRef ref) {
    return new TsLintState(ref, myNodePackageRef, myCustomConfigFileUsed, myCustomConfigFilePath, myRulesDirectory, myAllowJs);
  }

  public Builder builder() {
    return new Builder(this);
  }

  public static class Builder {
    private boolean myCustomConfigFileUsed = false;
    private String myCustomConfigFilePath = "";
    private NodeJsInterpreterRef myInterpreterRef = NodeJsInterpreterRef.createProjectRef();
    private NodePackageRef myNodePackageRef = NodePackageRef.create(new NodePackage(""));
    private boolean myAllowJs;

    @Nullable
    private String myRulesDirectory;

    public Builder() {
    }

    public Builder(@NotNull final TsLintState state) {
      myCustomConfigFileUsed = state.isCustomConfigFileUsed();
      myCustomConfigFilePath = state.getCustomConfigFilePath();
      myInterpreterRef = state.getInterpreterRef();
      myNodePackageRef = state.getNodePackageRef();
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

    public Builder setNodePackageRef(NodePackageRef nodePackageRef) {
      myNodePackageRef = nodePackageRef;
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
      return new TsLintState(myInterpreterRef, myNodePackageRef, myCustomConfigFileUsed, myCustomConfigFilePath, myRulesDirectory, myAllowJs);
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
    if (!Objects.equals(myNodePackageRef, state.myNodePackageRef)) return false;
    if (!Objects.equals(myCustomConfigFilePath, state.myCustomConfigFilePath)) return false;
    if (!Objects.equals(myRulesDirectory, state.myRulesDirectory)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myInterpreterRef.hashCode();
    result = 31 * result + myNodePackageRef.hashCode();
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
           ", myNodePackageRef='" + myNodePackageRef + '\'' +
           ", myCustomConfigFilePath='" + myCustomConfigFilePath + '\'' +
           ", myCustomConfigFileUsed=" + myCustomConfigFileUsed +
           ", myRulesDirectory='" + myRulesDirectory + '\'' +
           ", myAllowJs=" + myAllowJs +
           '}';
  }
}
