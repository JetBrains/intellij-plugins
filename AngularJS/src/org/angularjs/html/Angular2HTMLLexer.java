package org.angularjs.html;

import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.HtmlLexer;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class Angular2HTMLLexer extends HtmlLexer {
  private Lexer myInterpolationLexer;

  public Angular2HTMLLexer() {
    TokenHandler value = new TokenHandler() {
      @Override
      public void handleElement(Lexer lexer) {
        if (!isHtmlTagState(lexer.getState())) {
          final String text = lexer.getTokenText();
          if (text.startsWith("(") || text.startsWith("[")) {
            seenAttribute = true;
            seenScript = true;
          }
        }
      }
    };
    registerHandler(XmlTokenType.XML_NAME, value);
  }

  @Override
  public void advance() {
    if (myInterpolationLexer != null) {
      myInterpolationLexer.advance();
      try {
        if (myInterpolationLexer.getTokenType() != null) {
          return;
        }
      } catch (Error error) {
        Logger.getInstance(Angular2HTMLLexer.class).error(myInterpolationLexer.getBufferSequence());
      }
      myInterpolationLexer = null;
    }
    super.advance();
    final IElementType type = super.getTokenType();
    if (type == XmlTokenType.XML_DATA_CHARACTERS || type == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) {
      final Lexer lexer = createLexer(type);
      lexer.start(super.getTokenText());
      myInterpolationLexer = lexer;
    }
  }

  @Override
  public IElementType getTokenType() {
    if (myInterpolationLexer != null) {
      return myInterpolationLexer.getTokenType();
    }
    return super.getTokenType();
  }

  @Override
  public int getTokenStart() {
    if (myInterpolationLexer != null) {
      return super.getTokenStart() + myInterpolationLexer.getTokenStart();
    }
    return super.getTokenStart();
  }

  @Override
  public int getTokenEnd() {
    if (myInterpolationLexer != null) {
      return super.getTokenStart() + myInterpolationLexer.getTokenEnd();
    }
    return super.getTokenEnd();
  }

  @Override
  public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    myInterpolationLexer = null;
    super.start(buffer, startOffset, endOffset, initialState);
  }

  private static Lexer createLexer(IElementType type) {
    final _AngularJSInterpolationsLexer lexer = new _AngularJSInterpolationsLexer(null);
    lexer.setType(type);
    return new MergingLexerAdapter(new FlexAdapter(lexer), TokenSet.create(JSElementTypes.EMBEDDED_CONTENT, XmlTokenType.XML_DATA_CHARACTERS));
  }
}
