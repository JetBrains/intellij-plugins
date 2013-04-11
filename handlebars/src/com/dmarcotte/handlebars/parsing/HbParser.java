package com.dmarcotte.handlebars.parsing;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

class HbParser implements PsiParser {

  @NotNull
  public ASTNode parse(IElementType root, PsiBuilder builder) {
    final PsiBuilder.Marker rootMarker = builder.mark();

    new HbParsing(builder).parse();

    rootMarker.done(root);

    return builder.getTreeBuilt();
  }
}
