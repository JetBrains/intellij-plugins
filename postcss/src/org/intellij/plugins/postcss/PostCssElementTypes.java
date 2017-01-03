package org.intellij.plugins.postcss;

import com.intellij.psi.css.impl.stubs.CssStylesheetStubElementType;
import com.intellij.psi.css.impl.util.CssStylesheetLazyElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.intellij.plugins.postcss.psi.PostCssFileElementType;
import org.intellij.plugins.postcss.psi.stubs.types.PostCssCustomMediaStubElementType;
import org.intellij.plugins.postcss.psi.stubs.types.PostCssCustomSelectorStubElementType;

public interface PostCssElementTypes {

  // Stubs
  PostCssCustomSelectorStubElementType POST_CSS_CUSTOM_SELECTOR = new PostCssCustomSelectorStubElementType();
  PostCssCustomMediaStubElementType POST_CSS_CUSTOM_MEDIA = new PostCssCustomMediaStubElementType();

  IFileElementType POST_CSS_FILE = new PostCssFileElementType();
  IElementType POST_CSS_NEST = new PostCssElementType("POST_CSS_NEST");
  IElementType POST_CSS_CUSTOM_SELECTOR_RULE = new PostCssElementType("POST_CSS_CUSTOM_SELECTOR_RULE");
  IElementType POST_CSS_CUSTOM_MEDIA_RULE = new PostCssElementType("POST_CSS_CUSTOM_MEDIA_RULE");
  IElementType POST_CSS_APPLY_RULE = new PostCssElementType("POST_CSS_APPLY_RULE");
  CssStylesheetLazyElementType POST_CSS_LAZY_STYLESHEET = new CssStylesheetLazyElementType("POST_CSS_LAZY_STYLESHEET", PostCssLanguage.INSTANCE);
  CssStylesheetStubElementType POST_CSS_STYLESHEET = new CssStylesheetStubElementType("POST_CSS_STYLESHEET", PostCssLanguage.INSTANCE);
}
