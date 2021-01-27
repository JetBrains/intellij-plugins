// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class TsLintState implements JSNpmLinterState<TsLintState> {

  public static final TsLintState DEFAULT = new Builder().build();
  private final @NotNull NodePackageRef myNodePackageRef;
  private final @Nullable String myCustomConfigFilePath;

  private final boolean myCustomConfigFileUsed;
  private final @Nullable String myRulesDirectory;
  private final boolean myAllowJs;

  private TsLintState(@NotNull NodePackageRef nodePackageRef,
                      boolean customConfigFileUsed,
                      @Nullable String customConfigFilePath,
                      @Nullable String rulesDirectory, boolean allowJs) {
    myCustomConfigFileUsed = customConfigFileUsed;
    myCustomConfigFilePath = customConfigFilePath;
    myRulesDirectory = rulesDirectory;
    myAllowJs = allowJs;
    myNodePackageRef = nodePackageRef;
  }

  public boolean isCustomConfigFileUsed() {
    return myCustomConfigFileUsed;
  }

  public @Nullable String getCustomConfigFilePath() {
    return myCustomConfigFilePath;
  }

  public @Nullable String getRulesDirectory() {
    return myRulesDirectory;
  }

  public boolean isAllowJs() {
    return myAllowJs;
  }

  @Override
  public @NotNull NodePackageRef getNodePackageRef() {
    return myNodePackageRef;
  }

  @Override
  public TsLintState withLinterPackage(@NotNull NodePackageRef nodePackageRef) {
    return new Builder(this).setNodePackageRef(nodePackageRef).build();
  }

  public Builder builder() {
    return new Builder(this);
  }

  public static class Builder {
    private boolean myCustomConfigFileUsed = false;
    private String myCustomConfigFilePath = "";
    private NodePackageRef myNodePackageRef = NodePackageRef.create(new NodePackage(""));
    private boolean myAllowJs;

    private @Nullable String myRulesDirectory;

    public Builder() {
    }

    public Builder(final @NotNull TsLintState state) {
      myCustomConfigFileUsed = state.isCustomConfigFileUsed();
      myCustomConfigFilePath = state.getCustomConfigFilePath();
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
      return new TsLintState(myNodePackageRef, myCustomConfigFileUsed, myCustomConfigFilePath, myRulesDirectory, myAllowJs);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TsLintState state = (TsLintState)o;

    if (myCustomConfigFileUsed != state.myCustomConfigFileUsed) return false;
    if (myAllowJs != state.myAllowJs) return false;
    if (!Objects.equals(myNodePackageRef, state.myNodePackageRef)) return false;
    if (!Objects.equals(myCustomConfigFilePath, state.myCustomConfigFilePath)) return false;
    if (!Objects.equals(myRulesDirectory, state.myRulesDirectory)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myNodePackageRef.hashCode();
    result = 31 * result + (myCustomConfigFilePath != null ? myCustomConfigFilePath.hashCode() : 0);
    result = 31 * result + (myCustomConfigFileUsed ? 1 : 0);
    result = 31 * result + (myRulesDirectory != null ? myRulesDirectory.hashCode() : 0);
    result = 31 * result + (myAllowJs ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "TsLintState{" +
           "myNodePackageRef='" + myNodePackageRef + '\'' +
           ", myCustomConfigFilePath='" + myCustomConfigFilePath + '\'' +
           ", myCustomConfigFileUsed=" + myCustomConfigFileUsed +
           ", myRulesDirectory='" + myRulesDirectory + '\'' +
           ", myAllowJs=" + myAllowJs +
           '}';
  }
}
