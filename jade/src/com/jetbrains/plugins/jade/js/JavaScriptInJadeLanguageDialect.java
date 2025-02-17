// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.js;

import com.intellij.lang.DependentLanguage;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import org.jetbrains.annotations.NotNull;

public class JavaScriptInJadeLanguageDialect extends JSLanguageDialect implements DependentLanguage {
  public static final DialectOptionHolder DIALECT_OPTION_HOLDER = new DialectOptionHolder("JS_IN_JADE", false, true);

  public static final JavaScriptInJadeLanguageDialect INSTANCE = new JavaScriptInJadeLanguageDialect();

  private JavaScriptInJadeLanguageDialect() {
    super("JSInJade", DIALECT_OPTION_HOLDER);
  }

  @Override
  public boolean isAtLeast(@NotNull JSLanguageDialect other) {
    return super.isAtLeast(other) || JavaScriptSupportLoader.ECMA_SCRIPT_6.isAtLeast(other);
  }
}
