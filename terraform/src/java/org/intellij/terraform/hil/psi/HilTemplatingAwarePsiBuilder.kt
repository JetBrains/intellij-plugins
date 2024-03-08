// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lang.parser.GeneratedParserUtilBase;

public class HilTemplatingAwarePsiBuilder extends GeneratedParserUtilBase.Builder {
  public HilTemplatingAwarePsiBuilder(PsiBuilder builder,
                                      GeneratedParserUtilBase.ErrorState state_,
                                      PsiParser parser_) {
    super(builder, state_, parser_);
  }

  public boolean isTemplatingSupported() {
    return parser instanceof HILTemplateParser;
  }
}
