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
import com.intellij.psi.util.PsiTreeUtil;
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

    foldFileHeader(descriptors, (DartFile)root, document);                 // 1. File header
    foldImportExportStatements(descriptors, (DartFile)root);                     // 2. Import and export statements
  }

  protected String getLanguagePlaceholderText(@NotNull final ASTNode node, @NotNull final TextRange range) {
    final PsiElement psiElement = node.getPsi();
    if (psiElement instanceof DartFile) return "/.../";                           // 1. File header
    if (psiElement instanceof DartImportOrExportStatement) return "...";          // 2. Import and export statements

    return null;
  }

  protected boolean isRegionCollapsedByDefault(@NotNull final ASTNode node) {
    final PsiElement psiElement = node.getPsi();
    final CodeFoldingSettings settings = CodeFoldingSettings.getInstance();

    if (psiElement instanceof DartFile) return settings.COLLAPSE_FILE_HEADER;                // 1. File header
    if (psiElement instanceof DartImportOrExportStatement) return settings.COLLAPSE_IMPORTS; // 2. Import and export statements

    return false;
  }

  private static void foldFileHeader(final List<FoldingDescriptor> descriptors, final DartFile dartFile, final Document document) {
    final TextRange fileHeaderCommentsRange = getFileHeaderCommentsRange(dartFile);
    if (fileHeaderCommentsRange != null &&
        fileHeaderCommentsRange.getLength() > 1 &&
        document.getLineNumber(fileHeaderCommentsRange.getEndOffset()) > document.getLineNumber(fileHeaderCommentsRange.getStartOffset())) {
      descriptors.add(new FoldingDescriptor(dartFile, fileHeaderCommentsRange));
    }
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

  private static void foldImportExportStatements(final List<FoldingDescriptor> descriptors, final DartFile dartFile) {
    final DartImportOrExportStatement firstImport = PsiTreeUtil.findChildOfType(dartFile, DartImportOrExportStatement.class);
    if (firstImport == null) return;

    PsiElement lastImport = firstImport;
    PsiElement nextElement = firstImport;
    while (nextElement instanceof DartImportOrExportStatement ||
           nextElement instanceof PsiComment ||
           nextElement instanceof PsiWhiteSpace) {
      if (nextElement instanceof DartImportOrExportStatement) {
        lastImport = nextElement;
      }
      nextElement = nextElement.getNextSibling();
    }

    if (lastImport != firstImport) {
      final int startOffset = firstImport.getTextOffset() + firstImport.getFirstChild().getTextLength() + 1; // after "import " or "export "
      final int endOffset = lastImport.getTextRange().getEndOffset();
      descriptors.add(new FoldingDescriptor(firstImport, TextRange.create(startOffset, endOffset)));
    }
  }
}
