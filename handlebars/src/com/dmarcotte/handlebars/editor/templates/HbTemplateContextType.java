// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.editor.templates;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.HbHighlighter;
import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HbTemplateContextType extends TemplateContextType {
  private HbTemplateContextType() {
    super(HbBundle.message("template.context.name"));
  }

  @Override
  public boolean isInContext(@NotNull PsiFile file, int offset) {
    return HbLanguage.INSTANCE.is(file.getLanguage());
  }

  @Override
  public @Nullable SyntaxHighlighter createHighlighter() {
    return new HbHighlighter();
  }
}
