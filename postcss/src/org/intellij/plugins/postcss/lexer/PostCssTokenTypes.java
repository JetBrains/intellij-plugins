package org.intellij.plugins.postcss.lexer;

import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.intellij.plugins.postcss.PostCssElementType;

public interface PostCssTokenTypes {
  TokenSet POST_CSS_COMMENTS = CssElementTypes.COMMENTS;
  IElementType AMPERSAND = new PostCssElementType("POST_CSS_AMPERSAND");
  IElementType HASH_SIGN = new PostCssElementType("POST_CSS_HASH_SIGN");
  IElementType POST_CSS_NEST_SYM = new PostCssElementType("POST_CSS_NEST_SYM");
  IElementType POST_CSS_CUSTOM_SELECTOR_SYM = new PostCssElementType("POST_CSS_CUSTOM_SELECTOR_SYM");

  TokenSet KEYWORDS = TokenSet.create(POST_CSS_NEST_SYM, POST_CSS_CUSTOM_SELECTOR_SYM);
}
