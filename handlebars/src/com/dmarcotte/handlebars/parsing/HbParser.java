// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.parsing;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class HbParser implements PsiParser {

  @Override
  public @NotNull ASTNode parse(IElementType root, PsiBuilder builder) {
    final PsiBuilder.Marker rootMarker = builder.mark();

    getParsing(builder).parse();

    rootMarker.done(root);

    return builder.getTreeBuilt();
  }

  protected HbParsing getParsing(PsiBuilder builder) {
    return new HbParsing(builder);
  }
}
