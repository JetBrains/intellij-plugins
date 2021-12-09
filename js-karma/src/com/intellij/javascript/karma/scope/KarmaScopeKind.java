package com.intellij.javascript.karma.scope;

import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum KarmaScopeKind {
  ALL(JavaScriptBundle.messagePointer("rc.testRunScope.all")) {
    @NotNull
    @Override
    public KarmaScopeView createView(@NotNull Project project) {
      return new KarmaAllTestsScopeView();
    }
  },

  TEST_FILE(JavaScriptBundle.messagePointer("rc.testRunScope.testFile")) {
    @NotNull
    @Override
    public KarmaScopeView createView(@NotNull Project project) {
      return new KarmaTestFileScopeView(project);
    }
  },

  SUITE(JavaScriptBundle.messagePointer("rc.testRunScope.suite")) {
    @NotNull
    @Override
    public KarmaScopeView createView(@NotNull Project project) {
      return new KarmaSuiteOrTestScopeView(project,
                                           JavaScriptBundle.message("rc.testOrSuiteScope.suite.title"),
                                           JavaScriptBundle.message("rc.testOrSuiteScope.suite.label"));
    }
  },

  TEST(JavaScriptBundle.messagePointer("rc.testRunScope.test")) {
    @NotNull
    @Override
    public KarmaScopeView createView(@NotNull Project project) {
      return new KarmaSuiteOrTestScopeView(project,
                                           JavaScriptBundle.message("rc.testOrSuiteScope.test.title"),
                                           JavaScriptBundle.message("rc.testOrSuiteScope.test.label"));
    }
  };

  private final Supplier<@NotNull @Nls String> myNameSupplier;

  KarmaScopeKind(@NotNull Supplier<@NotNull @NlsSafe String> nameSupplier) {
    myNameSupplier = nameSupplier;
  }

  @NotNull
  public @Nls String getName() {
    return myNameSupplier.get();
  }

  @NotNull
  public abstract KarmaScopeView createView(@NotNull Project project);
}
