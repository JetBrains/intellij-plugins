package com.jetbrains.lang.dart.ide.runner.test;

import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunnerParameters;
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
      if (scope == Scope.GROUP || scope == Scope.METHOD) {
        myScope = Scope.GROUP_OR_TEST_BY_NAME;
      }
      else {
        myScope = scope;
      }
    }
  }

  /**
   * @return Test group name or individual test name
   */
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

  public enum Scope {
    FOLDER("All in folder"),
    FILE("All in file"),
    @Deprecated // GROUP_OR_TEST_BY_NAME used instead
      GROUP("Test group"),
    @Deprecated // GROUP_OR_TEST_BY_NAME used instead
      METHOD("Test name"),
    GROUP_OR_TEST_BY_NAME("Group or test by name");

    private final String myPresentableName;

    Scope(final String name) {
      myPresentableName = name;
    }

    public String getPresentableName() {
      return myPresentableName;
    }
  }
}
