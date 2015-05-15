package org.intellij.plugins.markdown.lang;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MarkdownFileType extends LanguageFileType {
  public static final MarkdownFileType INSTANCE = new MarkdownFileType();

  protected MarkdownFileType() {
    super(MarkdownLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Markdown";
  }

  @NotNull
  @Override
  public String getDescription() {
    return MarkdownBundle.message("markdown.file.type.description");
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "md";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return null;
  }
}
