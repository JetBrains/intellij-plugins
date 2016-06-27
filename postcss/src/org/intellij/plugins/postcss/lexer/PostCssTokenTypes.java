package org.intellij.plugins.postcss.lexer;

import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.postcss.PostCssElementType;

public interface PostCssTokenTypes {
  IElementType AMPERSAND = new PostCssElementType("POST_CSS_AMPERSAND");
}
