package com.intellij.javascript.karma.scope;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public enum KarmaScopeKind {
  ALL("&All tests") {
    @NotNull
    @Override
    public KarmaScopeView createView(@NotNull Project project) {
      return new KarmaAllScopeView();
    }
  },

  SUITE("&Suite") {
    @NotNull
    @Override
    public KarmaScopeView createView(@NotNull Project project) {
      return new KarmaSuiteOrTestScopeView("Edit suite name", "Suite name:");
    }
  },

  TEST("&Test") {
    @NotNull
    @Override
    public KarmaScopeView createView(@NotNull Project project) {
      return new KarmaSuiteOrTestScopeView("Edit test name", "Test name:");
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
