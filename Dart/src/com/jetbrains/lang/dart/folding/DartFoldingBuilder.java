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
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartTokenTypesSets;
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

    final TextRange fileHeaderRange = foldFileHeader(descriptors, (DartFile)root, document); // 1. File header
    foldImportExportStatements(descriptors, (DartFile)root);                                 // 2. Import and export statements
    foldComments(descriptors, root, fileHeaderRange);                                        // 3. Comments and comment sequences
    foldClassBodies(descriptors, (DartFile)root);                                            // 4. Class body
    // todo 5. Function body
  }

  protected String getLanguagePlaceholderText(@NotNull final ASTNode node, @NotNull final TextRange range) {
    final IElementType elementType = node.getElementType();
    final PsiElement psiElement = node.getPsi();

    if (psiElement instanceof DartFile) return "/.../";                              // 1.   File header
    if (psiElement instanceof DartImportOrExportStatement) return "...";             // 2.   Import and export statements
    if (elementType == DartTokenTypesSets.MULTI_LINE_DOC_COMMENT) return "/**...*/"; // 3.1. Multiline doc comments
    if (elementType == DartTokenTypesSets.MULTI_LINE_COMMENT) return "/*...*/";      // 3.2. Multiline comments
    if (elementType == DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT) return "///...";  // 3.3. Consequent single line doc comments
    if (elementType == DartTokenTypesSets.SINGLE_LINE_COMMENT) return "//...";       // 3.4. Consequent single line comments
    if (psiElement instanceof DartClassBody) return "{...}";                         // 4.   Class body

    return null;
  }

  protected boolean isRegionCollapsedByDefault(@NotNull final ASTNode node) {
    final IElementType elementType = node.getElementType();
    final PsiElement psiElement = node.getPsi();
    final CodeFoldingSettings settings = CodeFoldingSettings.getInstance();

    if (psiElement instanceof DartFile) return settings.COLLAPSE_FILE_HEADER;                // 1. File header
    if (psiElement instanceof DartImportOrExportStatement) return settings.COLLAPSE_IMPORTS; // 2. Import and export statements

    if (elementType == DartTokenTypesSets.MULTI_LINE_DOC_COMMENT ||                          // 3.1. Multiline doc comments
        elementType == DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT) {                         // 3.3. Consequent single line doc comments
      return settings.COLLAPSE_DOC_COMMENTS;
    }

    return false;
  }

  @Nullable
  private static TextRange foldFileHeader(final List<FoldingDescriptor> descriptors, final DartFile dartFile, final Document document) {
    final TextRange fileHeaderCommentsRange = getFileHeaderCommentsRange(dartFile);
    if (fileHeaderCommentsRange != null &&
        fileHeaderCommentsRange.getLength() > 1 &&
        document.getLineNumber(fileHeaderCommentsRange.getEndOffset()) > document.getLineNumber(fileHeaderCommentsRange.getStartOffset())) {
      descriptors.add(new FoldingDescriptor(dartFile, fileHeaderCommentsRange));
      return fileHeaderCommentsRange;
    }

    return null;
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

  private static void foldComments(final @NotNull List<FoldingDescriptor> descriptors,
                                   final @NotNull PsiElement root,
                                   final @Nullable TextRange fileHeaderRange) {
    for (PsiElement child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (fileHeaderRange != null && fileHeaderRange.intersects(child.getTextRange())) {
        continue;
      }

      if (child instanceof PsiComment) {
        final IElementType elementType = child.getNode().getElementType();
        if (elementType == DartTokenTypesSets.MULTI_LINE_DOC_COMMENT || elementType == DartTokenTypesSets.MULTI_LINE_COMMENT) {
          descriptors.add(new FoldingDescriptor(child, child.getTextRange()));
        }
        else if (elementType == DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT || elementType == DartTokenTypesSets.SINGLE_LINE_COMMENT) {
          final PsiElement firstCommentInSequence = child;
          PsiElement lastCommentInSequence = firstCommentInSequence;
          PsiElement nextElement = firstCommentInSequence;
          while ((nextElement = nextElement.getNextSibling()) != null &&
                 (nextElement instanceof PsiWhiteSpace || nextElement.getNode().getElementType() == elementType)) {
            if (nextElement.getNode().getElementType() == elementType) {
              lastCommentInSequence = nextElement;
            }
          }

          if (lastCommentInSequence != firstCommentInSequence) {
            final TextRange range =
              TextRange.create(firstCommentInSequence.getTextOffset(), lastCommentInSequence.getTextRange().getEndOffset());
            descriptors.add(new FoldingDescriptor(firstCommentInSequence, range));
          }

          // need to skip processed comments sequence
          //noinspection AssignmentToForLoopParameter
          child = lastCommentInSequence;
        }
      }
      else {
        foldComments(descriptors, child, fileHeaderRange);
      }
    }
  }

  private static void foldClassBodies(final List<FoldingDescriptor> descriptors, final DartFile dartFile) {
    for (DartClassDefinition dartClass : PsiTreeUtil.getChildrenOfTypeAsList(dartFile, DartClassDefinition.class)) {
      final DartClassBody body = dartClass.getClassBody();
      if (body != null && body.getTextLength() > 2) {
        descriptors.add(new FoldingDescriptor(body, body.getTextRange()));
      }
    }
  }
}
