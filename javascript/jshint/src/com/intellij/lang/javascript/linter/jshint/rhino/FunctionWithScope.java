package com.intellij.lang.javascript.linter.jshint.rhino;

import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
* @author Sergey Simonchik
*/
public class FunctionWithScope {
  private final Function myFunction;
  private final Scriptable myScope;

  FunctionWithScope(@NotNull Function function, @NotNull Scriptable scope) {
    this.myFunction = function;
    this.myScope = scope;
  }

  public @NotNull Function getFunction() {
    return myFunction;
  }

  public @NotNull Scriptable getScope() {
    return myScope;
  }
}
