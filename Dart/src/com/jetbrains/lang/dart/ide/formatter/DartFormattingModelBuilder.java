package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.FormatTextRanges;
import com.intellij.formatting.FormattingMode;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilderEx;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartFormattingModelBuilder implements FormattingModelBuilderEx {
  @NotNull
  @Override
  public FormattingModel createModel(@NotNull final PsiElement element, @NotNull final CodeStyleSettings settings) {
    return createModel(element, settings, FormattingMode.REFORMAT);
  }

  @NotNull
  @Override
  public FormattingModel createModel(@NotNull PsiElement element, @NotNull CodeStyleSettings settings, @NotNull FormattingMode mode) {
    // element can be DartFile, DartEmbeddedContent, DartExpressionCodeFragment
    final PsiFile psiFile = element.getContainingFile();
    final ASTNode rootNode = psiFile instanceof DartFile ? psiFile.getNode() : element.getNode();
    final DartBlockContext context = new DartBlockContext(settings, mode);
    final DartBlock rootBlock = new DartBlock(rootNode, null, null, settings, context);
    return new DocumentBasedFormattingModel(rootBlock, element.getProject(), settings, psiFile.getFileType(), psiFile);
  }

  @Nullable
  @Override
  public CommonCodeStyleSettings.IndentOptions getIndentOptionsToUse(@NotNull PsiFile file,
                                                                     @NotNull FormatTextRanges ranges,
                                                                     @NotNull CodeStyleSettings settings) {
    return null;
  }
}
