// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunnerParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DartTestRunnerParameters extends DartCommandLineRunnerParameters implements Cloneable {

  private @NotNull Scope myScope = Scope.FILE;
  private @Nullable String myTestName = null;
  private @Nullable String myTargetName = null;
  private @Nullable String myTestRunnerOptions = null;

  public @NotNull Scope getScope() {
    return myScope;
  }

  public void setScope(final Scope scope) {
    if (scope != null) { // null in case of corrupted storage
      //noinspection deprecation
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
  public @Nullable String getTestName() {
    return myTestName;
  }

  public void setTestName(final @Nullable String name) {
    myTestName = name;
  }

  public @Nullable String getTargetName() {
    return myTargetName;
  }

  public void setTargetName(final @Nullable String name) {
    myTargetName = name;
  }

  public @Nullable String getTestRunnerOptions() {
    return myTestRunnerOptions;
  }

  public void setTestRunnerOptions(@Nullable String testRunnerOptions) {
    myTestRunnerOptions = testRunnerOptions;
  }

  @Override
  public void check(@NotNull Project project) throws RuntimeConfigurationError {
    super.check(project);

    if (myScope == Scope.GROUP_OR_TEST_BY_NAME && StringUtil.isEmpty(myTestName)) {
      throw new RuntimeConfigurationError(DartBundle.message("dialog.message.group.or.test.name.not.specified"));
    }
  }

  @Override
  protected final DartTestRunnerParameters clone() {
    return (DartTestRunnerParameters)super.clone();
  }

  public enum Scope {
    FOLDER(DartBundle.messagePointer("test.mode.all.in.folder")),
    FILE(DartBundle.messagePointer("test.mode.all.in.file")),
    /**
     * @deprecated GROUP_OR_TEST_BY_NAME used instead
     */
    @Deprecated(forRemoval = true)
    GROUP(new Computable.PredefinedValueComputable<>("Test group")),
    /**
     * @deprecated GROUP_OR_TEST_BY_NAME used instead
     */
    @Deprecated(forRemoval = true)
    METHOD(new Computable.PredefinedValueComputable<>("Test name")),
    GROUP_OR_TEST_BY_NAME(DartBundle.messagePointer("test.mode.test.group.or.test.by.name")),
    MULTIPLE_NAMES(new Computable.PredefinedValueComputable<>("")); // Used by test re-runner action; not visible in UI

    private final Supplier<@NlsContexts.Label String> myPresentableNameSupplier;

    Scope(Supplier<@NlsContexts.Label String> nameSupplier) {
      myPresentableNameSupplier = nameSupplier;
    }

    public String getPresentableName() {
      return myPresentableNameSupplier.get();
    }
  }
}
