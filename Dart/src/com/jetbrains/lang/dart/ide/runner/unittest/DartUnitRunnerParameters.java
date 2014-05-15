package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.util.xmlb.annotations.MapAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class DartUnitRunnerParameters implements Cloneable {

  public enum Scope {METHOD, GROUP, ALL}

  private @NotNull Scope myScope = Scope.ALL;
  private @Nullable String myFilePath = null;
  private @Nullable String myTestName = null;
  private @Nullable String myVMOptions = null;
  private @Nullable String myArguments = null;
  private @Nullable String myWorkingDirectory = null;
  private @NotNull Map<String, String> myEnvs = new LinkedHashMap<String, String>();
  private boolean myIncludeParentEnvs = true;

  @NotNull
  public Scope getScope() {
    return myScope;
  }

  public void setScope(@SuppressWarnings("NullableProblems") final Scope scope) {
    if (scope != null) { // null in case of corrupted storage
      myScope = scope;
    }
  }

  @Nullable
  public String getFilePath() {
    return myFilePath;
  }

  public void setFilePath(final @Nullable String filePath) {
    myFilePath = filePath;
  }

  @Nullable
  public String getTestName() {
    return myTestName;
  }

  public void setTestName(final @Nullable String name) {
    myTestName = name;
  }

  @Nullable
  public String getVMOptions() {
    return myVMOptions;
  }

  public void setVMOptions(final @Nullable String VMOptions) {
    myVMOptions = VMOptions;
  }

  @Nullable
  public String getArguments() {
    return myArguments;
  }

  public void setArguments(final @Nullable String arguments) {
    myArguments = arguments;
  }

  @Nullable
  public String getWorkingDirectory() {
    return myWorkingDirectory;
  }

  public void setWorkingDirectory(final @Nullable String workingDirectory) {
    myWorkingDirectory = workingDirectory;
  }

  @NotNull
  @MapAnnotation(surroundWithTag = false, surroundKeyWithTag = false, surroundValueWithTag = false)
  public Map<String, String> getEnvs() {
    return myEnvs;
  }

  public void setEnvs(@SuppressWarnings("NullableProblems") final Map<String, String> envs) {
    if (envs != null) { // null comes from old projects or if storage corrupted
      myEnvs = envs;
    }
  }

  public boolean isIncludeParentEnvs() {
    return myIncludeParentEnvs;
  }

  public void setIncludeParentEnvs(final boolean includeParentEnvs) {
    myIncludeParentEnvs = includeParentEnvs;
  }

  @Override
  protected final DartUnitRunnerParameters clone() {
    try {
      final DartUnitRunnerParameters clone = (DartUnitRunnerParameters)super.clone();
      clone.myEnvs = new LinkedHashMap<String, String>();
      clone.myEnvs.putAll(myEnvs);
      return clone;
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
