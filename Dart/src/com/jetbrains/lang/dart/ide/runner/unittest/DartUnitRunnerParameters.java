package com.jetbrains.lang.dart.ide.runner.unittest;

import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunnerParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartUnitRunnerParameters extends DartCommandLineRunnerParameters implements Cloneable {

  public enum Scope {
    ALL("All in file"), GROUP("Test group"), METHOD("Single test");

    private final String myPresentableName;

    Scope(final String name) {
      myPresentableName = name;
    }

    public String getPresentableName() {
      return myPresentableName;
    }
  }

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
  protected final DartUnitRunnerParameters clone() {
    return (DartUnitRunnerParameters)super.clone();
  }
}
