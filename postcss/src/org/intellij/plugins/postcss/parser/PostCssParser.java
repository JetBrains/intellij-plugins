package org.intellij.plugins.postcss.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.css.impl.parsing.CssParser2;

public class PostCssParser extends CssParser2 {
  public PostCssParser(PsiBuilder builder) {
    super(builder);
  }
}
