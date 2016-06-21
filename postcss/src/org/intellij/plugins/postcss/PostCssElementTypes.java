package org.intellij.plugins.postcss;

import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.util.CssStyleSheetElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.intellij.plugins.postcss.psi.PostCssFileElementType;
import org.intellij.plugins.postcss.psi.PostCssStyleSheetElementType;

public interface PostCssElementTypes {
  TokenSet POST_CSS_COMMENTS = CssElementTypes.COMMENTS;
  IFileElementType POST_CSS_FILE = new PostCssFileElementType();

  CssStyleSheetElementType POST_CSS_STYLESHEET = new PostCssStyleSheetElementType();
}
