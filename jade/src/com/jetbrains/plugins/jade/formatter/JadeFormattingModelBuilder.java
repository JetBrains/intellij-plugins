// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlock;
import com.intellij.formatting.templateLanguages.TemplateLanguageFormattingModelBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class JadeFormattingModelBuilder extends TemplateLanguageFormattingModelBuilder {
  @Override
  public TemplateLanguageBlock createTemplateLanguageBlock(@NotNull ASTNode node,
                                                           @Nullable Wrap wrap,
                                                           @Nullable Alignment alignment,
                                                           @Nullable List<DataLanguageBlockWrapper> foreignChildren,
                                                           @NotNull CodeStyleSettings codeStyleSettings) {
    return new JadeBlock(node, wrap, alignment, this, codeStyleSettings, foreignChildren, Indent.getNoneIndent());
  }


  @Override
  public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    final PsiFile file = formattingContext.getContainingFile();
    CodeStyleSettings settings = formattingContext.getCodeStyleSettings();
    Block rootBlock = getRootBlock(formattingContext.getPsiElement(), file.getViewProvider(), settings);
    Document document = FormattingDocumentModelImpl.getDocumentToBeUsedFor(file);
    if (document != null && FormattingDocumentModelImpl.canUseDocumentModel(document, file)) {
      return new DocumentBasedFormattingModel(rootBlock, formattingContext.getProject(), settings, file.getFileType(), file);
    }
    return FormattingModelProvider.createFormattingModelForPsiFile(file, rootBlock, settings);
  }

  @Override
  public boolean dontFormatMyModel() {
    return false;
  }
}
