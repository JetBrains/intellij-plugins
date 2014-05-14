package com.jetbrains.lang.dart.ide.runner.unittest;

import org.jetbrains.annotations.Nullable;

public class DartUnitRunnerParameters implements Cloneable {
  @Nullable
  private String myFilePath = null;
  @Nullable
  private String myArguments = null;
  @Nullable
  private String myVMOptions = null;
  @Nullable
  private String myTestName = null;
  @Nullable
  private Scope myScope = Scope.ALL;
  @Nullable
  private String myWorkingDirectory = null;

  @Nullable
  public String getFilePath() { return myFilePath; }

  public void setFilePath(@Nullable String filePath) {
    myFilePath = filePath;
  }

  @Nullable
  public String getArguments() {
    return myArguments;
  }

  public void setArguments(@Nullable String arguments) {
    myArguments = arguments;
  }

  @Nullable
  public String getWorkingDirectory() { return myWorkingDirectory; }

  public void setWorkingDirectory(@Nullable String workingDirectory) { myWorkingDirectory = workingDirectory; }

  @Nullable
  public String getVMOptions() {
    return myVMOptions;
  }

  @Nullable
  public String getTestName() {
    return myTestName;
  }

  public void setTestName(@Nullable String name) {
    myTestName = name;
  }

  public void setVMOptions(@Nullable String VMOptions) {
    myVMOptions = VMOptions;
  }

  @Nullable
  public Scope getScope() {
    return myScope;
  }

  public void setScope(@Nullable Scope scope) {
    myScope = scope;
  }

  public enum Scope {
    METHOD, GROUP, ALL
  }

  @Override
  protected final DartUnitRunnerParameters clone() {
    try {
      return (DartUnitRunnerParameters)super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
