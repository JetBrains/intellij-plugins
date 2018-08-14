package org.angular2.lang.html;

import com.intellij.ide.highlighter.HtmlFileHighlighter;
import com.intellij.lexer.HtmlHighlightingLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;

public class Angular2HtmlHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory {
  @Override
  @NotNull
  protected SyntaxHighlighter createHighlighter() {
    return new Angular2HtmlHighlighter();
  }

  private static class Angular2HtmlHighlighter extends HtmlFileHighlighter {
    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
      return new Angular2HtmlHighlightingLexer();
    }

    private static class Angular2HtmlHighlightingLexer extends HtmlHighlightingLexer {
      Angular2HtmlHighlightingLexer() {
        super(FileTypeRegistry.getInstance().findFileTypeByName("CSS"));
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
    }
  }
}
