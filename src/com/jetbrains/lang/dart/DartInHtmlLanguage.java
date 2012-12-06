package com.jetbrains.lang.dart;

import com.intellij.lang.DependentLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.jetbrains.lang.dart.highlight.DartSyntaxHighlighter;
import org.jetbrains.annotations.NotNull;

public class DartInHtmlLanguage extends Language implements DependentLanguage {
  public static DartInHtmlLanguage INSTANCE = new DartInHtmlLanguage();

  protected DartInHtmlLanguage() {
    super(DartLanguage.INSTANCE, "Dart in Html");
  }

  {
    SyntaxHighlighterFactory.LANGUAGE_FACTORY.addExplicitExtension(this,
                                                                   new SingleLazyInstanceSyntaxHighlighterFactory() {
                                                                     @NotNull
                                                                     protected SyntaxHighlighter createHighlighter() {
                                                                       return new DartSyntaxHighlighter();
                                                                     }
                                                                   }
    );
    LanguageParserDefinitions.INSTANCE.addExplicitExtension(this, new DartParserDefinition());
  }
}
