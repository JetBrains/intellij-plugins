package org.intellij.plugins.markdown.lang;

import com.intellij.lang.Language;

public class MarkdownLanguage extends Language {

  public static final MarkdownLanguage INSTANCE = new MarkdownLanguage();

  protected MarkdownLanguage() {
    super("Markdown", "text/x-markdown");
  }
}
