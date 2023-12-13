// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.NotNull;

public final class DartFormattingModelBuilder implements FormattingModelBuilder {

  @Override
  public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    // element can be DartFile, DartEmbeddedContent, DartExpressionCodeFragment
    final PsiFile psiFile = formattingContext.getContainingFile();
    CodeStyleSettings settings = formattingContext.getCodeStyleSettings();
    final ASTNode rootNode = psiFile instanceof DartFile ? psiFile.getNode() : formattingContext.getNode();
    final DartBlockContext context = new DartBlockContext(settings, formattingContext.getFormattingMode());
    final DartBlock rootBlock = new DartBlock(rootNode, null, null, settings, context);
    return new DocumentBasedFormattingModel(rootBlock, formattingContext.getProject(), settings, psiFile.getFileType(), psiFile);
  }
}
