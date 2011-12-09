package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.lang.javascript.flex.run.LauncherParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FlexUnitCommonParameters {

  enum Scope {
    Method, Class, Package
  }

  enum OutputLogLevel {
    Fatal("FATAL"), Error("ERROR"), Warn("WARN"), Info("INFO"), Debug("DEBUG"), All("ALL");

    private final String myFlexConstant;

    OutputLogLevel(String flexConstant) {
      myFlexConstant = flexConstant;
    }

    public String getFlexConstant() {
      return myFlexConstant;
    }
  }

  @NotNull
  String getModuleName();

  void setModuleName(@NotNull final String moduleName);

  String getScopeRaw();

  void setScopeRaw(String scopeRaw);

  @NotNull
  Scope getScope();

  void setScope(@NotNull Scope scope);

  @NotNull
  String getPackageName();

  void setPackageName(@NotNull String packageName);

  @NotNull
  String getClassName();

  void setClassName(@NotNull String className);

  @NotNull
  String getMethodName();

  void setMethodName(@NotNull String methodName);

  int getPort();

  void setPort(int port);

  int getSocketPolicyPort();

  void setSocketPolicyPort(int port);

  String getOutputLogLevelRaw();

  void setOutputLogLevelRaw(String outputLogLevel);

  @Nullable
  OutputLogLevel getOutputLogLevel();

  void setOutputLogLevel(@Nullable OutputLogLevel outputLogLevel);

  LauncherParameters getLauncherParameters();
}
