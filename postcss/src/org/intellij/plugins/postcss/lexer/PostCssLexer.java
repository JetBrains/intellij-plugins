package org.intellij.plugins.postcss.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.tree.TokenSet;

public class PostCssLexer extends MergingLexerAdapter {
  private static final TokenSet TOKEN_SET = TokenSet.orSet(TokenSet.create(CssElementTypes.CSS_STRING_TOKEN),
                                                            CssElementTypes.COMMENTS);

  public PostCssLexer() {
    super(new FlexAdapter(new _PostCssLexer(null)), TOKEN_SET);
  }
}
