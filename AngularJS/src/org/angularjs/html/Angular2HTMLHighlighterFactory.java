package org.angularjs.html;

import com.intellij.ide.highlighter.HtmlFileHighlighter;
import com.intellij.lexer.HtmlHighlightingLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class Angular2HTMLHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory {
  @Override
  @NotNull
  protected SyntaxHighlighter createHighlighter() {
    return new Angular2HTMLHighlighter();
  }

  private static class Angular2HTMLHighlighter extends HtmlFileHighlighter {
    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
      return new Angular2HtmlHighlightingLexer();
    }

    private static class Angular2HtmlHighlightingLexer extends HtmlHighlightingLexer {
      public Angular2HtmlHighlightingLexer() {
        super(FileTypeRegistry.getInstance().findFileTypeByName("CSS"));
        TokenHandler value = new TokenHandler() {
          @Override
          public void handleElement(Lexer lexer) {
            final String text = lexer.getTokenText();
            if (text.startsWith("(") || text.startsWith("[")) {
              if (!isHtmlTagState(lexer.getState())) {
                seenAttribute = true;
                seenStyle = true;
              }
            }
          }
        };
        registerHandler(XmlTokenType.XML_NAME, value);
      }
    }
  }
}
