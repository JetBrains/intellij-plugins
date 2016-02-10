package com.jetbrains.lang.dart.ide.runner.test;

import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunnerParameters;
import com.jetbrains.lang.dart.ide.runner.util.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartTestRunnerParameters extends DartCommandLineRunnerParameters implements Cloneable {

  private @NotNull Scope myScope = Scope.FILE;
  private @Nullable String myTestName = null;
  private @Nullable String myTargetName = null;

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

  @Nullable
  public String getTargetName() {
    return myTargetName;
  }

  public void setTestName(final @Nullable String name) {
    myTestName = name;
  }

  public void setTargetName(final @Nullable String name) {
    myTargetName = name;
  }

  @Override
  protected final DartTestRunnerParameters clone() {
    return (DartTestRunnerParameters)super.clone();
  }
}
