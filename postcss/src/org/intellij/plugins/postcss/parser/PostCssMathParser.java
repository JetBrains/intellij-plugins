package org.intellij.plugins.postcss.parser;

import com.intellij.psi.css.impl.parsing.CssMathParser;
import com.intellij.psi.css.impl.parsing.CssParser;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.postcss.PostCssElementTypes;

public class PostCssMathParser extends CssMathParser {
  public PostCssMathParser(CssParser parser) {
    super(parser);
  }

  @Override
  protected boolean parseTerm(IElementType prevOperation) {
    if (getTokenType() == PostCssElementTypes.POST_CSS_SIMPLE_VARIABLE) {
      advance();
      return true;
    }
    else {
      return super.parseTerm(prevOperation);
    }
  }
}
