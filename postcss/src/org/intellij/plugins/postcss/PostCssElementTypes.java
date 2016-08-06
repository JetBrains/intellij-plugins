package org.intellij.plugins.postcss;

import com.intellij.psi.css.impl.util.CssStyleSheetElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.intellij.plugins.postcss.psi.PostCssFileElementType;
import org.intellij.plugins.postcss.psi.PostCssStyleSheetElementType;
import org.intellij.plugins.postcss.psi.stubs.types.PostCssCustomSelectorStubElementType;

public interface PostCssElementTypes {

  // Stubs
  PostCssCustomSelectorStubElementType POST_CSS_CUSTOM_SELECTOR = new PostCssCustomSelectorStubElementType();

  IFileElementType POST_CSS_FILE = new PostCssFileElementType();
  IElementType POST_CSS_NEST = new PostCssElementType("POST_CSS_NEST");
  IElementType POST_CSS_CUSTOM_SELECTOR_RULE = new PostCssElementType("POST_CSS_CUSTOM_SELECTOR_RULE");
  IElementType POST_CSS_CUSTOM_MEDIA_RULE = new PostCssElementType("POST_CSS_CUSTOM_MEDIA_RULE");
  CssStyleSheetElementType POST_CSS_STYLESHEET = new PostCssStyleSheetElementType();
}
