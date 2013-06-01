package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author fedor.korotkov
 */
public class DartFormattingModelBuilder implements FormattingModelBuilder {
  @NotNull
  @Override
  public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
    final PsiFile psiFile = element.getContainingFile();
    return new DartFormattingModel(
      psiFile,
      settings,
      new DartBlock(psiFile instanceof DartFile ? psiFile.getNode() : element.getNode(), null, null, settings)
    );
  }

  @Nullable
  @Override
  public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
    return null;
  }
}
