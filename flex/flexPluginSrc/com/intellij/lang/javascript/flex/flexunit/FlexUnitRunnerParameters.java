package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.lang.javascript.flex.run.AirRunnerParameters;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlexUnitRunnerParameters extends AirRunnerParameters {

  public enum Scope {
    Method, Class, Package
  }

  public enum OutputLogLevel {
    Fatal("FATAL"), Error("ERROR"), Warn("WARN"), Info("INFO"), Debug("DEBUG"), All("ALL");

    private final String myFlexConstant;

    OutputLogLevel(String flexConstant) {
      myFlexConstant = flexConstant;
    }

    public String getFlexConstant() {
      return myFlexConstant;
    }
  }

  private boolean myRunAsAir;

  private @NotNull String myPackageName = "";
  private @NotNull String myClassName = "";
  private @NotNull String myMethodName = "";

  private static final Scope DEFAULT_SCOPE = Scope.Class;

  private @NotNull Scope myScope = DEFAULT_SCOPE;

  private int myPort;

  private int mySocketPolicyPort;

  private static final OutputLogLevel DEFAULT_LEVEL = null;

  private @Nullable OutputLogLevel myOutputLogLevel = null;

  public FlexUnitRunnerParameters() {
  }

  @Transient
  public boolean isRunAsAir() {
    return myRunAsAir;
  }

  public void setRunAsAir(final boolean runAsAir) {
    myRunAsAir = runAsAir;
  }

  @Attribute("scope")
  public String getScopeRaw() {
    return myScope.name();
  }

  public void setScopeRaw(String scopeRaw) {
    try {
      myScope = Scope.valueOf(scopeRaw);
    }
    catch (IllegalArgumentException e) {
      myScope = DEFAULT_SCOPE;
    }
  }

  @NotNull
  @Transient
  public Scope getScope() {
    return myScope;
  }

  public void setScope(@NotNull Scope scope) {
    myScope = scope;
  }

  @NotNull
  @Attribute("package_name")
  public String getPackageName() {
    return myPackageName;
  }

  public void setPackageName(@NotNull String packageName) {
    myPackageName = packageName;
  }

  @NotNull
  @Attribute("class_name")
  public String getClassName() {
    return myClassName;
  }

  public void setClassName(@NotNull String className) {
    myClassName = className;
  }

  @NotNull
  @Attribute("method_name")
  public String getMethodName() {
    return myMethodName;
  }

  public void setMethodName(@NotNull String methodName) {
    myMethodName = methodName;
  }

  @Attribute("port")
  public int getPort() {
    return myPort;
  }

  public void setPort(int port) {
    myPort = port;
  }

  @Attribute("socket_policy_port")
  public int getSocketPolicyPort() {
    return mySocketPolicyPort;
  }

  public void setSocketPolicyPort(int port) {
    mySocketPolicyPort = port;
  }

  @Attribute("output_log_level")
  public String getOutputLogLevelRaw() {
    return myOutputLogLevel != null ? myOutputLogLevel.name() : "";
  }

  public void setOutputLogLevelRaw(String outputLogLevel) {
    if (StringUtil.isNotEmpty(outputLogLevel)) {
      try {
        myOutputLogLevel = OutputLogLevel.valueOf(outputLogLevel);
        return;
      }
      catch (IllegalArgumentException e) {
        // ignore
      }
    }
    myOutputLogLevel = DEFAULT_LEVEL;
  }

  @Nullable
  @Transient
  public OutputLogLevel getOutputLogLevel() {
    return myOutputLogLevel;
  }

  public void setOutputLogLevel(@Nullable OutputLogLevel outputLogLevel) {
    myOutputLogLevel = outputLogLevel;
  }

  @Override
  public FlexUnitRunnerParameters clone() {
    return (FlexUnitRunnerParameters)super.clone();
  }
}
