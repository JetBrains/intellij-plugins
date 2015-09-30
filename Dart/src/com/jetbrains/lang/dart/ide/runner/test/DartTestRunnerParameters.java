package com.jetbrains.lang.dart.ide.runner.test;

import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunnerParameters;
import com.jetbrains.lang.dart.ide.runner.util.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartTestRunnerParameters extends DartCommandLineRunnerParameters implements Cloneable {

  private @NotNull Scope myScope = Scope.ALL;
  private @Nullable String myTestName = null;

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

  @Override
  protected final DartTestRunnerParameters clone() {
    return (DartTestRunnerParameters)super.clone();
  }
}
