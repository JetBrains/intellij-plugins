// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.parsing;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class HbParser implements PsiParser {

  @Override
  @NotNull
  public ASTNode parse(IElementType root, PsiBuilder builder) {
    final PsiBuilder.Marker rootMarker = builder.mark();

    getParsing(builder).parse();

    rootMarker.done(root);

    return builder.getTreeBuilt();
  }

  protected HbParsing getParsing(PsiBuilder builder) {
    return new HbParsing(builder);
  }
}
