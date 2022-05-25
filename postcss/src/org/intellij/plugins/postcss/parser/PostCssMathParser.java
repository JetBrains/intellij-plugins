package org.intellij.plugins.postcss.parser;

import com.intellij.psi.css.impl.parsing.CssMathParser;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.jetbrains.annotations.Nullable;

public class PostCssMathParser extends CssMathParser {
  private final PostCssParser myParser;

  public PostCssMathParser(PostCssParser parser) {
    super(parser);
    myParser = parser;
  }

  @Override
  protected boolean parseTerm(@Nullable IElementType prevOperation) {
    if (getTokenType() == PostCssTokenTypes.POST_CSS_SIMPLE_VARIABLE_TOKEN) {
      myParser.parseSimpleVariable();
      return true;
    }
    else {
      return super.parseTerm(prevOperation);
    }
  }
}
