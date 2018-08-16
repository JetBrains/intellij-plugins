// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.inspections;


import com.dmarcotte.handlebars.file.HbFileViewProvider;
import com.intellij.codeInsight.highlighting.TemplateLanguageErrorFilter;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import static com.dmarcotte.handlebars.parsing.HbTokenTypes.*;

public class HbErrorFilter extends TemplateLanguageErrorFilter {

  private static final TokenSet START_TEMPLATE_TOKENS = TokenSet.create(OPEN, OPEN_PARTIAL, OPEN_BLOCK, OPEN_INVERSE);

  protected HbErrorFilter() {
    super(START_TEMPLATE_TOKENS, HbFileViewProvider.class, "HTML");
  }


  @Override
  protected boolean shouldIgnoreErrorAt(@NotNull FileViewProvider viewProvider, int offset) {
    if (super.shouldIgnoreErrorAt(viewProvider, offset)) return true;

    return hasWhitespacesInHtmlBetweenErrorAndOpenTokens(offset, (TemplateLanguageFileViewProvider)viewProvider);
  }

  private static boolean hasWhitespacesInHtmlBetweenErrorAndOpenTokens(int offset, TemplateLanguageFileViewProvider viewProvider) {
    PsiElement at = viewProvider.findElementAt(offset, viewProvider.getTemplateDataLanguage());
    if (!(at instanceof PsiWhiteSpace)) return false;
    PsiElement elementAt = viewProvider.findElementAt(at.getTextRange().getEndOffset() + 1, viewProvider.getBaseLanguage());
    if (elementAt != null && START_TEMPLATE_TOKENS.contains(elementAt.getNode().getElementType())) return true;

    return false;
  }
}
