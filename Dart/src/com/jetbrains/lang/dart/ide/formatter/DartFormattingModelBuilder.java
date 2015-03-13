package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartFormattingModelBuilder implements FormattingModelBuilder {
  @NotNull
  @Override
  public FormattingModel createModel(@NotNull final PsiElement element, @NotNull final CodeStyleSettings settings) {
    // element can be DartFile, DartEmbeddedContent, DartExpressionCodeFragment
    final PsiFile psiFile = element.getContainingFile();
    final ASTNode rootNode = psiFile instanceof DartFile ? psiFile.getNode() : element.getNode();
    final DartBlock rootBlock = new DartBlock(rootNode, null, null, settings);
    return new DocumentBasedFormattingModel(rootBlock, element.getProject(), settings, psiFile.getFileType(), psiFile);
  }

  @Nullable
  @Override
  public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
    return null;
  }
}
