package com.jetbrains.lang.dart.folding;

import com.intellij.codeInsight.folding.CodeFoldingSettings;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UnfairTextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartFoldingBuilder extends CustomFoldingBuilder implements DumbAware {
  protected void buildLanguageFoldRegions(@NotNull final List<FoldingDescriptor> descriptors,
                                          @NotNull final PsiElement root,
                                          @NotNull final Document document,
                                          final boolean quick) {
    if (!(root instanceof DartFile)) return;

    // 1. File header
    final TextRange fileHeaderCommentsRange = getFileHeaderCommentsRange((DartFile)root);
    if (fileHeaderCommentsRange != null &&
        fileHeaderCommentsRange.getLength() > 1 &&
        document.getLineNumber(fileHeaderCommentsRange.getEndOffset()) > document.getLineNumber(fileHeaderCommentsRange.getStartOffset())) {
      descriptors.add(new FoldingDescriptor(root, fileHeaderCommentsRange));
    }
  }

  protected String getLanguagePlaceholderText(@NotNull final ASTNode node, @NotNull final TextRange range) {
    final PsiElement psiElement = node.getPsi();
    if (psiElement instanceof DartFile) return "/.../"; // 1. File header

    return null;
  }

  protected boolean isRegionCollapsedByDefault(@NotNull final ASTNode node) {
    final PsiElement psiElement = node.getPsi();
    final CodeFoldingSettings settings = CodeFoldingSettings.getInstance();

    if (psiElement instanceof DartFile) return settings.COLLAPSE_FILE_HEADER; // 1. File header

    return false;
  }

  @Nullable
  private static TextRange getFileHeaderCommentsRange(@NotNull final DartFile file) {
    PsiElement firstComment = file.getFirstChild();
    if (firstComment instanceof PsiWhiteSpace) firstComment = firstComment.getNextSibling();

    if (!(firstComment instanceof PsiComment)) return null;

    PsiElement nextAfterComments = firstComment;
    while (nextAfterComments instanceof PsiComment || nextAfterComments instanceof PsiWhiteSpace) {
      nextAfterComments = nextAfterComments.getNextSibling();
    }

    if (nextAfterComments == null) return null;

    if (nextAfterComments instanceof DartLibraryStatement ||
        nextAfterComments instanceof DartPartStatement ||
        nextAfterComments instanceof DartPartOfStatement ||
        nextAfterComments instanceof DartImportOrExportStatement) {
      if (nextAfterComments.getPrevSibling() instanceof PsiWhiteSpace) nextAfterComments = nextAfterComments.getPrevSibling();
      if (nextAfterComments == null || nextAfterComments.equals(firstComment)) return null;
      return new UnfairTextRange(firstComment.getTextOffset(), nextAfterComments.getTextOffset());
    }
    else {
      // this is not a file-level comment, but class, function or var doc
      return null;
    }
  }
}
