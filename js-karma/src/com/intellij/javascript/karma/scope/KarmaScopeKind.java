// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.scope;

import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum KarmaScopeKind {
  ALL(JavaScriptBundle.messagePointer("rc.testRunScope.all")) {
    @Override
    public @NotNull KarmaScopeView createView(@NotNull Project project) {
      return new KarmaAllTestsScopeView();
    }
  },

  TEST_FILE(JavaScriptBundle.messagePointer("rc.testRunScope.testFile")) {
    @Override
    public @NotNull KarmaScopeView createView(@NotNull Project project) {
      return new KarmaTestFileScopeView(project);
    }
  },

  SUITE(JavaScriptBundle.messagePointer("rc.testRunScope.suite")) {
    @Override
    public @NotNull KarmaScopeView createView(@NotNull Project project) {
      return new KarmaSuiteOrTestScopeView(project,
                                           JavaScriptBundle.message("rc.testOrSuiteScope.suite.title"),
                                           JavaScriptBundle.message("rc.testOrSuiteScope.suite.label"));
    }
  },

  TEST(JavaScriptBundle.messagePointer("rc.testRunScope.test")) {
    @Override
    public @NotNull KarmaScopeView createView(@NotNull Project project) {
      return new KarmaSuiteOrTestScopeView(project,
                                           JavaScriptBundle.message("rc.testOrSuiteScope.test.title"),
                                           JavaScriptBundle.message("rc.testOrSuiteScope.test.label"));
    }
  };

  private final Supplier<@NotNull @Nls String> myNameSupplier;

  KarmaScopeKind(@NotNull Supplier<@NotNull @NlsSafe String> nameSupplier) {
    myNameSupplier = nameSupplier;
  }

  public @NotNull @Nls String getName() {
    return myNameSupplier.get();
  }

  public abstract @NotNull KarmaScopeView createView(@NotNull Project project);
}
