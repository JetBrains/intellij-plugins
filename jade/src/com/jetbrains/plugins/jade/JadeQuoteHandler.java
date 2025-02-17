package com.jetbrains.plugins.jade;

import com.intellij.javascript.JSQuoteHandler;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;

public final class JadeQuoteHandler extends JSQuoteHandler {
  public JadeQuoteHandler() {
    super(TokenSet.orSet(JSTokenTypes.STRING_LITERALS));
  }

  @Override
  public boolean isAppropriateElementTypeForLiteral(@NotNull IElementType tokenType) {
    if (super.isAppropriateElementTypeForLiteral(tokenType)) {
      return true;
    }

    if (tokenType == JadeTokenTypes.RPAREN
      || tokenType == JadeTokenTypes.ATTRIBUTE_NAME
      || tokenType == JadeTokenTypes.COMMA
      || tokenType == JadeTokenTypes.EOL
      || tokenType == JadeTokenTypes.INDENT
      || tokenType == JadeTokenTypes.TEXT) {
      return true;
    }
    return false;
  }
}
