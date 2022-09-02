package org.intellij.plugins.postcss;

import com.intellij.psi.css.impl.stubs.CssStylesheetStubElementType;
import org.intellij.plugins.postcss.psi.stubs.types.PostCssCustomMediaStubElementType;
import org.intellij.plugins.postcss.psi.stubs.types.PostCssCustomSelectorStubElementType;

public interface PostCssStubElementTypes {
  CssStylesheetStubElementType POST_CSS_STYLESHEET = new CssStylesheetStubElementType("POST_CSS_STYLESHEET", PostCssLanguage.INSTANCE);
  PostCssCustomSelectorStubElementType POST_CSS_CUSTOM_SELECTOR = new PostCssCustomSelectorStubElementType("POST_CSS_CUSTOM_SELECTOR");
  PostCssCustomMediaStubElementType POST_CSS_CUSTOM_MEDIA = new PostCssCustomMediaStubElementType("POST_CSS_CUSTOM_MEDIA");
}
