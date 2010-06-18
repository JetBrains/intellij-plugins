package com.intellij.tapestry.lang;

import com.intellij.lang.Language;
import com.intellij.lang.InjectableLanguage;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 *         Date: Jun 22, 2009
 *         Time: 8:44:31 PM
 */
public class TelLanguage extends Language implements InjectableLanguage {

  public static final TelLanguage INSTANCE = new TelLanguage();

  private TelLanguage() {
    super("TEL");

    SyntaxHighlighterFactory.LANGUAGE_FACTORY.addExplicitExtension(this, new SingleLazyInstanceSyntaxHighlighterFactory() {
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
