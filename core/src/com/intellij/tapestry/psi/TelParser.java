package com.intellij.tapestry.psi;

import com.intellij.lang.PsiParser;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
* @author Alexey Chmutov
*         Date: Jun 22, 2009
*         Time: 9:53:11 PM
*/
public class TelParser implements PsiParser {
  @NotNull
  public ASTNode parse(IElementType root, PsiBuilder builder) {
    final ASTNode contextNode = builder.getUserData(TelElementTypes.TAP5_CONTEXT_NODE_KEY);
    final PsiBuilder.Marker rootMarker = builder.mark();
    final boolean elUnderFile = contextNode != null && contextNode.getElementType() == TelElementTypes.TEL_FILE;
    final PsiBuilder.Marker markerUnderFile = elUnderFile ? builder.mark() : null;
    while (!builder.eof()) {
      parseExpression(builder);
    }
    if (markerUnderFile != null) {
      markerUnderFile.done(root);
      rootMarker.done(TelElementTypes.TEL_FILE);
    }
    else {
      rootMarker.done(root);
    }
    return builder.getTreeBuilt();
  }

  public static void parseExpression(PsiBuilder builder) {
    final IElementType tokenType = builder.getTokenType();
    if (tokenType == TelElementTypes.TAP5_EL_START ||
        tokenType == TelElementTypes.TAP5_EL_END) {
      builder.advanceLexer();
    }
    parseExpressionInner(builder);
  }

  private static void parseExpressionInner(PsiBuilder builder) {
    builder.getTokenType(); // todo
    builder.advanceLexer();
  }
}
