package com.intellij.javascript.karma.scope;

import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public enum KarmaScopeKind {
  ALL(JavaScriptBundle.message("rc.testRunScope.all")) {
    @NotNull
    @Override
    public KarmaScopeView createView(@NotNull Project project) {
      return new KarmaAllTestsScopeView();
    }
  },

  TEST_FILE(JavaScriptBundle.message("rc.testRunScope.testFile")) {
    @NotNull
    @Override
    public KarmaScopeView createView(@NotNull Project project) {
      return new KarmaTestFileScopeView(project);
    }
  },

  SUITE(JavaScriptBundle.message("rc.testRunScope.suite")) {
    @NotNull
    @Override
    public KarmaScopeView createView(@NotNull Project project) {
      return new KarmaSuiteOrTestScopeView(project,
                                           JavaScriptBundle.message("rc.testOrSuiteScope.suite.title"),
                                           JavaScriptBundle.message("rc.testOrSuiteScope.suite.label"));
    }
  },

  TEST(JavaScriptBundle.message("rc.testRunScope.test")) {
    @NotNull
    @Override
    public KarmaScopeView createView(@NotNull Project project) {
      return new KarmaSuiteOrTestScopeView(project,
                                           JavaScriptBundle.message("rc.testOrSuiteScope.test.title"),
                                           JavaScriptBundle.message("rc.testOrSuiteScope.test.label"));
    }
  };

  private final String myName;

  KarmaScopeKind(@NotNull String name) {
    myName = name;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public abstract KarmaScopeView createView(@NotNull Project project);
}
