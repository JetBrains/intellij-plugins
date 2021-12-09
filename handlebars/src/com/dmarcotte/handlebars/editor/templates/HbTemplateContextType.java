package com.dmarcotte.handlebars.editor.templates;


import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.HbHighlighter;
import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HbTemplateContextType extends TemplateContextType {
  protected HbTemplateContextType() {
    super("Handlebars", HbBundle.message("template.context.name"));
  }

  @Override
  public boolean isInContext(@NotNull PsiFile file, int offset) {
    return HbLanguage.INSTANCE.is(file.getLanguage());
  }

  @Nullable
  @Override
  public SyntaxHighlighter createHighlighter() {
    return new HbHighlighter();
  }
}
