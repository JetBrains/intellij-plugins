package com.jetbrains.lang.dart.ide.runner.test;

import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunnerParameters;
import com.jetbrains.lang.dart.ide.runner.util.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartTestRunnerParameters extends DartCommandLineRunnerParameters implements Cloneable {

  @NotNull private Scope myScope = Scope.FILE;
  @Nullable private String myTestName = null;
  @Nullable private String myTargetName = null;
  @Nullable private String myTestRunnerOptions = null;

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
  public String getTestName() {
    return myTestName;
  }

  public void setTestName(final @Nullable String name) {
    myTestName = name;
  }

  @Nullable
  public String getTargetName() {
    return myTargetName;
  }

  public void setTargetName(final @Nullable String name) {
    myTargetName = name;
  }

  @Nullable
  public String getTestRunnerOptions() {
    return myTestRunnerOptions;
  }

  public void setTestRunnerOptions(@Nullable String testRunnerOptions) {
    myTestRunnerOptions = testRunnerOptions;
  }

  @Override
  protected final DartTestRunnerParameters clone() {
    return (DartTestRunnerParameters)super.clone();
  }
}
