package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.lang.javascript.flex.run.LauncherParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FlexUnitCommonParameters {

  @NotNull
  String getModuleName();

  void setModuleName(@NotNull final String moduleName);

  String getScopeRaw();

  void setScopeRaw(String scopeRaw);

  @NotNull
  NewFlexUnitRunnerParameters.Scope getScope();

  void setScope(@NotNull NewFlexUnitRunnerParameters.Scope scope);

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
  NewFlexUnitRunnerParameters.OutputLogLevel getOutputLogLevel();

  void setOutputLogLevel(@Nullable NewFlexUnitRunnerParameters.OutputLogLevel outputLogLevel);

  LauncherParameters getLauncherParameters();
}
