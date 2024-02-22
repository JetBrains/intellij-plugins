// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.codeInsight.editorActions.moveUpDown.LineMover;
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiDocumentManagerBase;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.intellij.terraform.hcl.HCLLanguage;
import org.intellij.terraform.hcl.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Set;

/**
 * Based on com.intellij.codeInsight.editorActions.moveUpDown.StatementMover
 */
public class HCLStatementMover extends LineMover {
  private static final Logger LOG = Logger.getInstance(HCLStatementMover.class);

  @Override
  public boolean checkAvailable(@NotNull final Editor editor,
                                @NotNull final PsiFile file,
                                @NotNull final MoveInfo info,
                                final boolean down) {
    final boolean available = super.checkAvailable(editor, file, info, down);
    if (!available) return false;
    LineRange range = info.toMove;

    range = expandLineRangeToCoverPsiElements(range, editor, file);
    if (range == null) return false;
    info.toMove = range;
    final int startOffset = editor.logicalPositionToOffset(new LogicalPosition(range.startLine, 0));
    final int endOffset = editor.logicalPositionToOffset(new LogicalPosition(range.endLine, 0));
    final PsiElement[] statements = findStatementsInRange(file, startOffset, endOffset);
    if (statements.length == 0) return false;
    range.firstElement = statements[0];
    range.lastElement = statements[statements.length - 1];

    final PsiElement element = range.firstElement.getParent();
    final PsiElement next = down ? getNextSiblingElement(element) : getPrevSiblingElement(element);
    if (next != null) {
      info.toMove2 = new LineRange(next);
      return true;
    }
    return info.prohibitMove();
  }

  private static PsiElement getNextSiblingElement(final PsiElement element) {
    if (element instanceof HCLBlock || element instanceof HCLProperty) {
      final PsiElement next = HCLPsiUtil.getNextSiblingNonWhiteSpace(element);
      return (next != null && !next.getText().equals("}")) ? next : null;
    }
    return null;
  }

  private static PsiElement getPrevSiblingElement(final PsiElement element) {
    if (element instanceof HCLBlock || element instanceof HCLProperty) {
      final PsiElement prev = HCLPsiUtil.getPrevSiblingNonWhiteSpace(element);
      return (prev != null && !prev.getText().equals("{")) ? prev : null;
    }
    return null;
  }

  private static LineRange expandLineRangeToCoverPsiElements(final LineRange range, Editor editor, final PsiFile file) {
    Pair<PsiElement, PsiElement> psiRange = getElementRange(editor, file, range);
    if (psiRange == null) return null;
    final PsiElement parent = PsiTreeUtil.findCommonParent(psiRange.getFirst(), psiRange.getSecond());
    Pair<PsiElement, PsiElement> elementRange = getElementRange(parent, psiRange.getFirst(), psiRange.getSecond());
    if (elementRange == null) return null;
    int endOffset = elementRange.getSecond().getTextRange().getEndOffset();
    Document document = editor.getDocument();
    if (endOffset > document.getTextLength()) {
      LOG.assertTrue(!PsiDocumentManager.getInstance(file.getProject()).isUncommited(document));
      LOG.assertTrue(PsiDocumentManagerBase.checkConsistency(file, document));
    }
    int endLine;
    if (endOffset == document.getTextLength()) {
      endLine = document.getLineCount();
    }
    else {
      endLine = editor.offsetToLogicalPosition(endOffset).line + 1;
      endLine = Math.min(endLine, document.getLineCount());
    }
    int startLine = Math.min(range.startLine, editor.offsetToLogicalPosition(elementRange.getFirst().getNode().getStartOffset()).line);
    endLine = Math.max(endLine, range.endLine);
    return new LineRange(startLine, endLine);
  }

  @Nullable
  private static Language findHCLOrLikeLanguage(@NotNull final PsiFile file) {
    final Set<Language> languages = file.getViewProvider().getLanguages();
    for (final Language language : languages) {
      if (language == HCLLanguage.INSTANCE) return language;
    }
    for (final Language language : languages) {
      if (language.isKindOf(HCLLanguage.INSTANCE)) return language;
    }
    return null;
  }

  private static PsiElement @NotNull [] findStatementsInRange(@NotNull PsiFile file, int startOffset, int endOffset) {
    Language language = findHCLOrLikeLanguage(file);
    if (language == null) return PsiElement.EMPTY_ARRAY;
    FileViewProvider viewProvider = file.getViewProvider();
    PsiElement element1 = viewProvider.findElementAt(startOffset, language);
    PsiElement element2 = viewProvider.findElementAt(endOffset - 1, language);
    if (element1 instanceof PsiWhiteSpace) {
      startOffset = element1.getTextRange().getEndOffset();
      element1 = file.findElementAt(startOffset);
    }
    if (element2 instanceof PsiWhiteSpace) {
      endOffset = element2.getTextRange().getStartOffset();
      element2 = file.findElementAt(endOffset - 1);
    }
    if (element1 == null || element2 == null) return PsiElement.EMPTY_ARRAY;

    PsiElement parent = PsiTreeUtil.findCommonParent(element1, element2);
    if (parent == null) return PsiElement.EMPTY_ARRAY;
    while (true) {
      if (parent instanceof HCLProperty) break;
      if (parent instanceof HCLBlock) break;
      if (parent instanceof HCLArray) break;
      if (parent == null || parent instanceof PsiFile) return PsiElement.EMPTY_ARRAY;
      parent = parent.getParent();
    }

    //region Take outer possible child elements
    if (!parent.equals(element1)) {
      while (!parent.equals(element1.getParent())) {
        element1 = element1.getParent();
      }
    }
    if (startOffset != element1.getTextRange().getStartOffset()) return PsiElement.EMPTY_ARRAY;

    if (!parent.equals(element2)) {
      while (!parent.equals(element2.getParent())) {
        element2 = element2.getParent();
      }
    }
    if (endOffset != element2.getTextRange().getEndOffset()) return PsiElement.EMPTY_ARRAY;
    //endregion

    // Get all children in between
    ArrayList<PsiElement> array = new ArrayList<>();
    PsiElement next = element1;
    while (next != null) {
      if (!(next instanceof PsiWhiteSpace)) {
        array.add(next);
      }
      if (next.equals(element2)) break;
      next = next.getNextSibling();
    }
    // Ensure only proper elements would be returned
    for (PsiElement element : array) {
      if (!(element instanceof HCLElement ||
            element instanceof PsiWhiteSpace ||
            element instanceof PsiComment ||
            element instanceof LeafPsiElement)) {
        return PsiElement.EMPTY_ARRAY;
      }
    }
    return PsiUtilCore.toPsiElementArray(array);
  }
}

