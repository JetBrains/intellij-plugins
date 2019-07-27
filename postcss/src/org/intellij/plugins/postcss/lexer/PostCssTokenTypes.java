package org.intellij.plugins.postcss.lexer;

import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.intellij.plugins.postcss.PostCssCommentElementType;
import org.intellij.plugins.postcss.PostCssElementType;

public interface PostCssTokenTypes {
  IElementType AMPERSAND = new PostCssElementType("POST_CSS_AMPERSAND");
  IElementType HASH_SIGN = new PostCssElementType("POST_CSS_HASH_SIGN");
  IElementType POST_CSS_NEST_SYM = new PostCssElementType("POST_CSS_NEST_SYM");
  IElementType POST_CSS_CUSTOM_SELECTOR_SYM = new PostCssElementType("POST_CSS_CUSTOM_SELECTOR_SYM");
  IElementType POST_CSS_CUSTOM_MEDIA_SYM = new PostCssElementType("POST_CSS_CUSTOM_MEDIA_SYM");
  IElementType LT = new PostCssElementType("POST_CSS_LT");
  IElementType LE = new PostCssElementType("POST_CSS_LE");
  IElementType GE = new PostCssElementType("POST_CSS_GE");
  IElementType POST_CSS_COMMENT = new PostCssCommentElementType("POST_CSS_LINE_COMMENT");

  IElementType POST_CSS_SIMPLE_VARIABLE_TOKEN = new PostCssElementType("POST_CSS_SIMPLE_VARIABLE_TOKEN");

  TokenSet KEYWORDS = TokenSet.create(POST_CSS_NEST_SYM, POST_CSS_CUSTOM_SELECTOR_SYM, POST_CSS_CUSTOM_MEDIA_SYM);
  TokenSet IDENTIFIERS = TokenSet.create(AMPERSAND, HASH_SIGN);
  // only comparison operators allowed in media feature range (see https://www.w3.org/TR/mediaqueries-4/#typedef-mf-range)
  TokenSet COMPARISON_OPERATORS = TokenSet.create(CssElementTypes.CSS_EQ, CssElementTypes.CSS_GT, LT, GE, LE);
  TokenSet POST_CSS_COMMENTS = TokenSet.orSet(CssElementTypes.COMMENTS, TokenSet.create(POST_CSS_COMMENT));
}
