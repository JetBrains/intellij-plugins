package com.intellij.coldFusion;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.coldFusion.model.files.CfmlFileViewProvider;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class CfmlTemplateContextType extends TemplateContextType {
  protected CfmlTemplateContextType() {
    super("ColdFusion", "ColdFusion");
  }

  @Override
  public boolean isInContext(@NotNull PsiFile file, int offset) {
    return file.getViewProvider() instanceof CfmlFileViewProvider;
  }
}
