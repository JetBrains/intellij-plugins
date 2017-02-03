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
import org.angularjs.lang.parser.AngularJSElementTypes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class Angular2HTMLLexer extends HtmlLexer {
  private static final int SEEN_ANGULAR_SCRIPT = 0x1000;
  private Lexer myInterpolationLexer;
  private int interpolationStart = -1;
  private boolean seenAngularScript;

  public Angular2HTMLLexer() {
    TokenHandler value = new TokenHandler() {
      @Override
      public void handleElement(Lexer lexer) {
        if (!isHtmlTagState(lexer.getState())) {
          final String text = lexer.getTokenText();
          if (text.startsWith("(") || text.startsWith("[") || text.startsWith("*")) {
            seenAttribute = true;
            seenScript = true;
            seenAngularScript = true;
          } else  {
            seenAngularScript = false;
          }
        }
      }
    };
    registerHandler(XmlTokenType.XML_NAME, value);
    final TokenHandler scriptCleaner = new TokenHandler() {
      @Override
      public void handleElement(Lexer lexer) {
        seenAngularScript = false;
      }
    };
    registerHandler(XmlTokenType.XML_TAG_END, scriptCleaner);
    registerHandler(XmlTokenType.XML_END_TAG_START, scriptCleaner);
    registerHandler(XmlTokenType.XML_EMPTY_ELEMENT_END, scriptCleaner);
    registerHandler(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, scriptCleaner);
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
      interpolationStart = -1;
      return;
    }
    super.advance();
    final IElementType originalType = super.getTokenType();
    if (originalType == XmlTokenType.XML_DATA_CHARACTERS || originalType == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) {
      IElementType type = originalType;
      interpolationStart = super.getTokenStart();
      final StringBuilder text = new StringBuilder();
      while (type == XmlTokenType.XML_DATA_CHARACTERS ||
             type == XmlTokenType.XML_REAL_WHITE_SPACE ||
             type == XmlTokenType.XML_WHITE_SPACE ||
             type == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN ||
             type == XmlTokenType.XML_CHAR_ENTITY_REF ||
             type == XmlTokenType.XML_ENTITY_REF_TOKEN) {
        text.append(super.getTokenText());
        super.advance();
        type = getTokenType();
      }
      final Lexer lexer = createLexer(originalType);
      lexer.start(text);
      myInterpolationLexer = lexer;
    }
  }

  @Override
  public IElementType getTokenType() {
    final IElementType type = super.getTokenType();
    if (type == JSElementTypes.EMBEDDED_CONTENT && seenAngularScript) {
      return AngularJSElementTypes.EMBEDDED_CONTENT;
    }
    if (myInterpolationLexer != null) {
      return myInterpolationLexer.getTokenType();
    }
    return type;
  }

  @Override
  public int getTokenStart() {
    if (myInterpolationLexer != null) {
      return interpolationStart + myInterpolationLexer.getTokenStart();
    }
    return super.getTokenStart();
  }

  @Override
  public int getTokenEnd() {
    if (myInterpolationLexer != null) {
      return interpolationStart + myInterpolationLexer.getTokenEnd();
    }
    return super.getTokenEnd();
  }

  @Override
  public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    myInterpolationLexer = null;
    interpolationStart = -1;
    seenAngularScript = (initialState & SEEN_ANGULAR_SCRIPT) != 0;
    super.start(buffer, startOffset, endOffset, initialState);
  }

  @Override
  public int getState() {
    final int state = super.getState();
    return state | ((seenAngularScript) ? SEEN_ANGULAR_SCRIPT : 0);
  }


  private static Lexer createLexer(IElementType type) {
    final _AngularJSInterpolationsLexer lexer = new _AngularJSInterpolationsLexer(null);
    lexer.setType(type);
    return new MergingLexerAdapter(new FlexAdapter(lexer), TokenSet.create(AngularJSElementTypes.EMBEDDED_CONTENT,
                                                                           XmlTokenType.XML_DATA_CHARACTERS,
                                                                           XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN));
  }
}
