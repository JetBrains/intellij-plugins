package org.intellij.plugins.postcss;

import com.intellij.psi.css.impl.util.CssStylesheetLazyElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.intellij.plugins.postcss.psi.PostCssFileElementType;

public interface PostCssElementTypes {

  IFileElementType POST_CSS_FILE = new PostCssFileElementType();
  IElementType POST_CSS_NEST = new PostCssElementType("POST_CSS_NEST");
  IElementType POST_CSS_CUSTOM_SELECTOR_RULE = new PostCssElementType("POST_CSS_CUSTOM_SELECTOR_RULE");
  IElementType POST_CSS_CUSTOM_MEDIA_RULE = new PostCssElementType("POST_CSS_CUSTOM_MEDIA_RULE");
  CssStylesheetLazyElementType POST_CSS_LAZY_STYLESHEET = new CssStylesheetLazyElementType("POST_CSS_LAZY_STYLESHEET", PostCssLanguage.INSTANCE);

  IElementType POST_CSS_SIMPLE_VARIABLE = new PostCssElementType("POST_CSS_SIMPLE_VARIABLE");
  IElementType POST_CSS_SIMPLE_VARIABLE_DECLARATION = new PostCssElementType("POST_CSS_SIMPLE_VARIABLE_DECLARATION");
}
