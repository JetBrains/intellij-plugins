// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

/*
 * @author max
 */
package com.intellij.lang.javascript.refactoring;

import com.intellij.lang.javascript.JSKeywordSets;
import com.intellij.lang.javascript.dialects.ECMAL4LanguageDialect;
import org.jetbrains.annotations.NotNull;

public class ECMAL4NamesValidator extends JSNamesValidator {
  private static final ECMAL4NamesValidator DEFAULT_INSTANCE = new ECMAL4NamesValidator();

  public ECMAL4NamesValidator() {
    super(ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER);
  }

  public static boolean isIdentifier(@NotNull String name) {
    return DEFAULT_INSTANCE.isToken(name, JSKeywordSets.AS_IDENTIFIER_TOKENS_SET);
  }
}