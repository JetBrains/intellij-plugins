// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.codeInsight.editorActions.moveUpDown.LineMover;
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerBase;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.terraform.hcl.HCLElementTypes;
import org.intellij.terraform.hcl.HCLLanguage;
import org.intellij.terraform.hcl.psi.HCLBlock;
import org.intellij.terraform.hcl.psi.HCLProperty;
import org.intellij.terraform.hcl.psi.HCLPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Based on com.intellij.codeInsight.editorActions.moveUpDown.StatementMover
 */
public class HCLStatementMover extends LineMover {
  private static final Logger LOG = Logger.getInstance(HCLStatementMover.class);

  @Override
  public boolean checkAvailable(final @NotNull Editor editor,
                                final @NotNull PsiFile file,
                                final @NotNull MoveInfo info,
                                final boolean down) {
    final boolean available = super.checkAvailable(editor, file, info, down);
    if (!available) return false;
    LineRange range = getLineRangeFromSelection(editor);

    range = expandLineRangeToCoverPsiElements(range, editor, file);
    if (range == null) return false;
    info.toMove = range;

    final Pair<PsiElement, PsiElement> psiElements = getElementsInRange(range, editor, file);
    if (psiElements == null) return false;
    final var element = down ? psiElements.second : psiElements.first;
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
      return (next != null && next.getNode().getElementType() != HCLElementTypes.R_CURLY) ? next : null;
    }
    return null;
  }

  private static PsiElement getPrevSiblingElement(final PsiElement element) {
    if (element instanceof HCLBlock || element instanceof HCLProperty) {
      final PsiElement prev = HCLPsiUtil.getPrevSiblingNonWhiteSpace(element);
      return (prev != null && prev.getNode().getElementType() != HCLElementTypes.L_CURLY) ? prev : null;
    }
    return null;
  }

  private static Pair<PsiElement, PsiElement> getElementsInRange(final LineRange range, Editor editor, final PsiFile file) {
    Language language = findHCLOrLikeLanguage(file);
    if (language == null) return null;
    Pair<PsiElement, PsiElement> psiRange = getElementRange(editor, file, range);
    if (psiRange == null) return null;
    final PsiElement parent = PsiTreeUtil.findCommonParent(psiRange.getFirst(), psiRange.getSecond());
    return getElementRange(parent, psiRange.getFirst(), psiRange.getSecond());
  }

  private static LineRange expandLineRangeToCoverPsiElements(final LineRange range, Editor editor, final PsiFile file) {
    Pair<PsiElement, PsiElement> elementRange = getElementsInRange(range, editor, file);
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

  private static @Nullable Language findHCLOrLikeLanguage(final @NotNull PsiFile file) {
    final Set<Language> languages = file.getViewProvider().getLanguages();
    for (final Language language : languages) {
      if (language == HCLLanguage.INSTANCE) return language;
    }
    for (final Language language : languages) {
      if (language.isKindOf(HCLLanguage.INSTANCE)) return language;
    }
    return null;
  }
}

