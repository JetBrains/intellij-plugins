package com.intellij.lang.javascript.linter.jshint.config;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class JSHintOptionCompletionObject {
  private final String myName;

  public JSHintOptionCompletionObject(@NotNull String name) {
    myName = StringUtil.stripQuotesAroundValue(name);
  }

  public @NotNull String getName() {
    return myName;
  }
}
