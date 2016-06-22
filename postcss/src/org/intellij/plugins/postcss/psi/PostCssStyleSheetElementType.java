package org.intellij.plugins.postcss.psi;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.util.CssStyleSheetElementType;
import org.intellij.plugins.postcss.PostCSSLanguage;

public class PostCssStyleSheetElementType extends CssStyleSheetElementType {
  public PostCssStyleSheetElementType() {
    super("POST_CSS_STYLESHEET", PostCSSLanguage.INSTANCE);
  }

  @Override
  protected Language getLanguageForParser(PsiElement psi) {
    return getLanguage();
  }
}
