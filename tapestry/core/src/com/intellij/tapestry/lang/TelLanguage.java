package com.intellij.tapestry.lang;

import com.intellij.lang.InjectableLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class TelLanguage extends Language implements InjectableLanguage {

  public static final TelLanguage INSTANCE = new TelLanguage();

  private TelLanguage() {
    super("TEL");

    SyntaxHighlighterFactory.LANGUAGE_FACTORY.addExplicitExtension(this, new SingleLazyInstanceSyntaxHighlighterFactory() {
      @Override
      @NotNull
      protected SyntaxHighlighter createHighlighter() {
        return new TelHighlighter();
      }
    });
  }

  @Override
  public TelFileType getAssociatedFileType() {
    return TelFileType.INSTANCE;
  }
}
